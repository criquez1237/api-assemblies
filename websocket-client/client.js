const WebSocket = require('ws');
const readline = require('readline');
const colors = require('colors');

class AssembliesWebSocketClient {
    constructor(authToken = null) {
        this.sockets = {};
        this.isConnected = false;
        this.authToken = authToken;
        
        // Configurar la interfaz de readline para entrada del usuario
        this.rl = readline.createInterface({
            input: process.stdin,
            output: process.stdout
        });
    }

    // Conectar a un endpoint específico
    connect(endpoint = 'general') {
        const wsUrl = `ws://localhost:8081/api/ws/${endpoint}`;
        
        console.log(`🔌 Conectando a: ${wsUrl}`.yellow);
        
        // Configurar headers para autenticación si hay token
        const wsOptions = {};
        if (this.authToken) {
            wsOptions.headers = {
                'Authorization': `Bearer ${this.authToken}`
            };
            console.log('🔑 Usando token de autenticación'.green);
        }
        
        const ws = new WebSocket(wsUrl, wsOptions);
        
        ws.on('open', () => {
            console.log(`✅ Conectado a ${endpoint.toUpperCase()} channel!`.green);
            this.isConnected = true;
            this.sockets[endpoint] = ws;
            this.showMenu();
        });

        ws.on('message', (data) => {
            try {
                const message = JSON.parse(data.toString());
                this.handleMessage(endpoint, message);
            } catch (error) {
                console.log(`📨 [${endpoint.toUpperCase()}] Raw message:`.cyan, data.toString());
            }
        });

        ws.on('close', (code, reason) => {
            console.log(`❌ Conexión cerrada [${endpoint.toUpperCase()}]: ${code} - ${reason}`.red);
            this.isConnected = false;
            delete this.sockets[endpoint];
        });

        ws.on('error', (error) => {
            console.log(`🚫 Error en conexión [${endpoint.toUpperCase()}]:`.red, error.message);
        });

        return ws;
    }

    // Manejar mensajes recibidos
    handleMessage(endpoint, message) {
        const timestamp = new Date().toLocaleTimeString();
        
        switch (message.type) {
            case 'WELCOME':
                console.log(`🎉 [${timestamp}] Bienvenida:`.green, message.message);
                break;
                
            case 'PONG':
                console.log(`🏓 [${timestamp}] PONG recibido`.magenta);
                break;
                
            case 'ORDER_STATUS_UPDATE':
                console.log(`📦 [${timestamp}] Actualización de Orden:`.blue);
                console.log(`   Título: ${message.title}`);
                console.log(`   Mensaje: ${message.message}`);
                if (message.data) {
                    console.log(`   Orden ID: ${message.data.orderId}`);
                    console.log(`   Estado: ${message.data.oldStatus} → ${message.data.newStatus}`);
                }
                break;
                
            case 'OUT_OF_STOCK':
                console.log(`⚠️  [${timestamp}] ALERTA - Producto Agotado:`.red);
                console.log(`   ${message.title}: ${message.message}`);
                if (message.data) {
                    console.log(`   Producto ID: ${message.data.productId}`);
                    console.log(`   Nombre: ${message.data.productName}`);
                }
                break;
                
            case 'STOCK_UPDATE':
                console.log(`📊 [${timestamp}] Actualización de Stock:`.yellow);
                if (message.data) {
                    // Formato de NotificationMessage con datos en el campo 'data'
                    console.log(`   Producto: ${message.data.productName || message.title}`);
                    console.log(`   Stock Anterior: ${message.data.previousStock}`);
                    console.log(`   Stock Actual: ${message.data.currentStock}`);
                    console.log(`   Cambio: ${message.data.stockChange}`);
                    console.log(`   Tipo: ${message.data.changeType}`);
                } else if (message.productName) {
                    // Formato de StockUpdateMessage directo
                    console.log(`   Producto: ${message.productName}`);
                    console.log(`   Stock Anterior: ${message.previousStock}`);
                    console.log(`   Stock Actual: ${message.currentStock}`);
                    console.log(`   Cambio: ${message.stockChange}`);
                    console.log(`   Tipo: ${message.changeType}`);
                } else {
                    // Fallback para otros formatos
                    console.log(`   Título: ${message.title}`);
                    console.log(`   Mensaje: ${message.message}`);
                }
                break;
                
            case 'productId': // Para mensajes de StockUpdateMessage directos
                console.log(`📊 [${timestamp}] Actualización de Stock (Directo):`.yellow);
                console.log(`   Producto: ${message.productName}`);
                console.log(`   Stock Anterior: ${message.previousStock}`);
                console.log(`   Stock Actual: ${message.currentStock}`);
                console.log(`   Cambio: ${message.stockChange}`);
                console.log(`   Tipo: ${message.changeType}`);
                console.log(`   Razón: ${message.reason}`);
                break;
                
            case 'SUBSCRIPTION_CONFIRMED':
                console.log(`✅ [${timestamp}] Suscripción confirmada:`.green, message.channel);
                break;
                
            case 'ERROR':
                console.log(`❌ [${timestamp}] Error:`.red, message.message);
                break;
                
            default:
                // Verificar si es un StockUpdateMessage sin type específico
                if (message.productId && message.productName && typeof message.previousStock !== 'undefined') {
                    console.log(`📊 [${timestamp}] Actualización de Stock (StockUpdateMessage):`.yellow);
                    console.log(`   Producto: ${message.productName}`);
                    console.log(`   Stock Anterior: ${message.previousStock}`);
                    console.log(`   Stock Actual: ${message.currentStock}`);
                    console.log(`   Cambio: ${message.stockChange}`);
                    console.log(`   Tipo: ${message.changeType}`);
                    console.log(`   Razón: ${message.reason}`);
                } else {
                    console.log(`📨 [${timestamp}] [${endpoint.toUpperCase()}] Mensaje:`.cyan);
                    console.log(JSON.stringify(message, null, 2));
                }
        }
    }

    // Enviar mensaje
    send(endpoint, message) {
        if (this.sockets[endpoint] && this.sockets[endpoint].readyState === WebSocket.OPEN) {
            const messageStr = typeof message === 'string' ? message : JSON.stringify(message);
            this.sockets[endpoint].send(messageStr);
            console.log(`📤 Mensaje enviado a ${endpoint}:`.green, messageStr);
        } else {
            console.log(`❌ No hay conexión activa a ${endpoint}`.red);
        }
    }

    // Mostrar menú interactivo
    showMenu() {
        console.log('\n' + '='.repeat(50).cyan);
        console.log('🎮 MENÚ INTERACTIVO - ASSEMBLIES WEBSOCKET'.cyan.bold);
        console.log('='.repeat(50).cyan);
        console.log('📋 Comandos disponibles:'.yellow);
        console.log('  ping              - Enviar ping al servidor');
        console.log('  subscribe <canal> - Suscribirse a un canal');
        console.log('  unsubscribe <canal> - Desuscribirse de un canal');
        console.log('  message <texto>   - Enviar mensaje de texto');
        console.log('  connect <endpoint> - Conectar a otro endpoint (general/notifications/stock)');
        console.log('  token <jwt_token> - Establecer token de autenticación');
        console.log('  stats             - Ver estadísticas de conexiones');
        console.log('  help              - Mostrar este menú');
        console.log('  exit              - Salir del cliente');
        console.log('='.repeat(50).cyan);
        
        this.promptUser();
    }

    // Prompt para entrada del usuario
    promptUser() {
        this.rl.question('\n🎯 Ingresa un comando: '.green, (input) => {
            this.processCommand(input.trim());
        });
    }

    // Procesar comandos del usuario
    processCommand(command) {
        const parts = command.split(' ');
        const cmd = parts[0].toLowerCase();
        const args = parts.slice(1);

        switch (cmd) {
            case 'ping':
                this.sendPing();
                break;
                
            case 'subscribe':
                if (args.length > 0) {
                    this.subscribe(args[0]);
                } else {
                    console.log('❌ Uso: subscribe <canal>'.red);
                }
                break;
                
            case 'unsubscribe':
                if (args.length > 0) {
                    this.unsubscribe(args[0]);
                } else {
                    console.log('❌ Uso: unsubscribe <canal>'.red);
                }
                break;
                
            case 'message':
                if (args.length > 0) {
                    this.sendTextMessage(args.join(' '));
                } else {
                    console.log('❌ Uso: message <texto>'.red);
                }
                break;
                
            case 'connect':
                if (args.length > 0) {
                    this.connect(args[0]);
                } else {
                    console.log('❌ Uso: connect <general|notifications|stock>'.red);
                }
                break;
                
            case 'token':
                if (args.length > 0) {
                    this.setToken(args[0]);
                } else {
                    console.log('❌ Uso: token <jwt_token>'.red);
                }
                break;
                
            case 'stats':
                this.requestStats();
                break;
                
            case 'help':
                this.showMenu();
                return; // No llamar promptUser() de nuevo
                
            case 'exit':
                this.disconnect();
                return;
                
            default:
                console.log(`❌ Comando desconocido: ${cmd}`.red);
                console.log('💡 Escribe "help" para ver comandos disponibles'.yellow);
        }
        
        this.promptUser();
    }

    // Comandos específicos
    sendPing() {
        const endpoint = Object.keys(this.sockets)[0] || 'general';
        this.send(endpoint, { type: 'PING' });
    }

    subscribe(channel) {
        const endpoint = Object.keys(this.sockets)[0] || 'general';
        this.send(endpoint, { type: 'SUBSCRIBE', channel: channel });
    }

    unsubscribe(channel) {
        const endpoint = Object.keys(this.sockets)[0] || 'general';
        this.send(endpoint, { type: 'UNSUBSCRIBE', channel: channel });
    }

    sendTextMessage(text) {
        const endpoint = Object.keys(this.sockets)[0] || 'general';
        this.send(endpoint, text);
    }

    requestStats() {
        console.log('📊 Para ver estadísticas, usa la API REST:'.yellow);
        console.log('   curl -H "Authorization: Bearer <token>" http://localhost:8081/api/realtime/stats');
    }

    // Establecer token de autenticación
    setToken(token) {
        this.authToken = token;
        console.log('✅ Token de autenticación establecido'.green);
        console.log(`🔑 Token: ${token.substring(0, 20)}...`.gray);
    }

    // Desconectar todos los sockets
    disconnect() {
        console.log('👋 Cerrando conexiones...'.yellow);
        
        Object.keys(this.sockets).forEach(endpoint => {
            if (this.sockets[endpoint]) {
                this.sockets[endpoint].close();
            }
        });
        
        this.rl.close();
        console.log('✅ Cliente WebSocket cerrado'.green);
        process.exit(0);
    }
}

// Función principal
function main() {
    console.log('🚀 ASSEMBLIES STORE - CLIENTE WEBSOCKET'.rainbow.bold);
    console.log('🔗 Conectando al servidor WebSocket...'.cyan);
    
    // Obtener token de argumentos o variable de entorno
    const authToken = process.argv[3] || process.env.AUTH_TOKEN;
    const client = new AssembliesWebSocketClient(authToken);
    
    if (authToken) {
        console.log('🔑 Token de autenticación detectado'.green);
    }
    
    // Manejar cierre del proceso
    process.on('SIGINT', () => {
        console.log('\n💫 Cerrando cliente...'.yellow);
        client.disconnect();
    });
    
    // Conectar por defecto al endpoint general
    const endpoint = process.argv[2] || 'general';
    client.connect(endpoint);
}

// Ejecutar si es el archivo principal
if (require.main === module) {
    main();
}

module.exports = AssembliesWebSocketClient;
