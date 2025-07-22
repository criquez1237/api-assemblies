const WebSocket = require('ws');
const colors = require('colors');

class NotificationClient {
    constructor(userId, authToken) {
        this.userId = userId;
        this.authToken = authToken;
        this.ws = null;
        this.notifications = [];
        this.isConnected = false;
    }

    connect() {
        const wsUrl = 'ws://localhost:8081/api/ws/notifications';
        
        console.log('🔔 Conectando al sistema de notificaciones...'.yellow);
        
        this.ws = new WebSocket(wsUrl, {
            headers: this.authToken ? {
                'Authorization': `Bearer ${this.authToken}`
            } : {}
        });

        this.ws.on('open', () => {
            console.log('✅ Cliente de notificaciones conectado!'.green);
            this.isConnected = true;
            this.subscribeToNotifications();
        });

        this.ws.on('message', (data) => {
            try {
                const message = JSON.parse(data.toString());
                this.handleNotification(message);
            } catch (error) {
                console.log('📨 Mensaje raw:', data.toString());
            }
        });

        this.ws.on('close', (code, reason) => {
            console.log(`❌ Conexión cerrada: ${code} - ${reason}`.red);
            this.isConnected = false;
        });

        this.ws.on('error', (error) => {
            console.log('🚫 Error:', error.message.red);
        });
    }

    handleNotification(message) {
        const timestamp = new Date().toLocaleString();
        
        // Agregar a historial
        this.notifications.push({
            ...message,
            timestamp: timestamp
        });

        switch (message.type) {
            case 'ORDER_STATUS_UPDATE':
                this.displayOrderUpdate(timestamp, message);
                break;
                
            case 'OUT_OF_STOCK':
                this.displayOutOfStock(timestamp, message);
                break;
                
            case 'WELCOME':
                console.log(`🎉 [${timestamp}] ${message.message}`.green);
                break;
                
            case 'SUBSCRIPTION_CONFIRMED':
                console.log(`✅ [${timestamp}] Suscrito a: ${message.channel}`.green);
                break;
                
            default:
                this.displayGenericNotification(timestamp, message);
        }
    }

    displayOrderUpdate(timestamp, message) {
        console.log('\n' + '📦'.repeat(15).blue);
        console.log(`📦 ACTUALIZACIÓN DE ORDEN - ${timestamp}`.blue.bold);
        console.log('📦'.repeat(15).blue);
        
        console.log(`📋 Título: ${message.title}`.white.bold);
        console.log(`💬 Mensaje: ${message.message}`.white);
        
        if (message.data) {
            console.log(`🆔 Orden ID: ${message.data.orderId}`.gray);
            
            if (message.data.oldStatus && message.data.newStatus) {
                const oldStatus = message.data.oldStatus;
                const newStatus = message.data.newStatus;
                let statusColor = 'white';
                
                switch (newStatus) {
                    case 'PAID':
                        statusColor = 'green';
                        break;
                    case 'CANCELLED':
                    case 'FAILED':
                        statusColor = 'red';
                        break;
                    case 'PROCESSING':
                        statusColor = 'yellow';
                        break;
                    case 'SHIPPED':
                        statusColor = 'blue';
                        break;
                }
                
                console.log(`🔄 Estado: ${oldStatus} → ${newStatus}`[statusColor]);
            }
            
            if (message.data.amount) {
                console.log(`💰 Monto: $${message.data.amount}`.green);
            }
        }
        
        console.log('📦'.repeat(15).blue + '\n');
    }

    displayOutOfStock(timestamp, message) {
        console.log('\n' + '⚠️'.repeat(15).red);
        console.log(`⚠️  ALERTA DE STOCK - ${timestamp}`.red.bold);
        console.log('⚠️'.repeat(15).red);
        
        console.log(`📋 ${message.title}`.white.bold);
        console.log(`💬 ${message.message}`.white);
        
        if (message.data) {
            console.log(`🆔 Producto ID: ${message.data.productId}`.gray);
            console.log(`🏷️  Producto: ${message.data.productName}`.white);
            console.log(`📦 Stock: ${message.data.currentStock || 0}`.red);
        }
        
        console.log('⚠️'.repeat(15).red + '\n');
    }

    displayGenericNotification(timestamp, message) {
        console.log('\n' + '🔔'.repeat(15).cyan);
        console.log(`🔔 NOTIFICACIÓN - ${timestamp}`.cyan.bold);
        console.log('🔔'.repeat(15).cyan);
        
        if (message.title) {
            console.log(`📋 ${message.title}`.white.bold);
        }
        
        if (message.message) {
            console.log(`💬 ${message.message}`.white);
        }
        
        if (message.data) {
            console.log('📊 Datos:'.gray);
            console.log(JSON.stringify(message.data, null, 2).gray);
        }
        
        console.log('🔔'.repeat(15).cyan + '\n');
    }

    subscribeToNotifications() {
        const subscribeMessage = {
            type: 'SUBSCRIBE',
            channel: 'user_notifications'
        };
        
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(subscribeMessage));
            console.log('🔔 Suscrito a notificaciones de usuario'.green);
        }
    }

    subscribeToChannel(channel) {
        const subscribeMessage = {
            type: 'SUBSCRIBE',
            channel: channel
        };
        
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(subscribeMessage));
            console.log(`🔔 Suscrito al canal: ${channel}`.green);
        }
    }

    sendPing() {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify({ type: 'PING' }));
            console.log('🏓 Ping enviado'.magenta);
        }
    }

    showNotificationHistory() {
        console.log('\n' + '📚'.repeat(20).yellow);
        console.log('📚 HISTORIAL DE NOTIFICACIONES'.yellow.bold);
        console.log('📚'.repeat(20).yellow);
        
        if (this.notifications.length === 0) {
            console.log('📭 No hay notificaciones en el historial'.gray);
        } else {
            this.notifications.forEach((notification, index) => {
                console.log(`\n${index + 1}. [${notification.timestamp}] ${notification.type}`.cyan);
                if (notification.title) {
                    console.log(`   📋 ${notification.title}`.white);
                }
                if (notification.message) {
                    console.log(`   💬 ${notification.message}`.gray);
                }
            });
        }
        
        console.log('\n' + '📚'.repeat(20).yellow + '\n');
    }

    showStats() {
        const stats = {
            totalNotifications: this.notifications.length,
            orderUpdates: this.notifications.filter(n => n.type === 'ORDER_STATUS_UPDATE').length,
            stockAlerts: this.notifications.filter(n => n.type === 'OUT_OF_STOCK').length,
            connected: this.isConnected
        };
        
        console.log('\n' + '📊'.repeat(20).blue);
        console.log('📊 ESTADÍSTICAS DEL CLIENTE'.blue.bold);
        console.log('📊'.repeat(20).blue);
        console.log(`🔗 Estado: ${stats.connected ? 'Conectado'.green : 'Desconectado'.red}`);
        console.log(`📨 Total notificaciones: ${stats.totalNotifications}`.white);
        console.log(`📦 Actualizaciones de orden: ${stats.orderUpdates}`.yellow);
        console.log(`⚠️  Alertas de stock: ${stats.stockAlerts}`.red);
        console.log('📊'.repeat(20).blue + '\n');
    }

    disconnect() {
        if (this.ws) {
            this.ws.close();
        }
    }
}

// Función para interacción por consola
function startInteractiveMode(client) {
    const readline = require('readline');
    const rl = readline.createInterface({
        input: process.stdin,
        output: process.stdout
    });

    function showMenu() {
        console.log('\n' + '🎮'.repeat(15).cyan);
        console.log('🎮 MENÚ INTERACTIVO'.cyan.bold);
        console.log('🎮'.repeat(15).cyan);
        console.log('1. ping          - Enviar ping');
        console.log('2. subscribe     - Suscribirse a canal');
        console.log('3. history       - Ver historial');
        console.log('4. stats         - Ver estadísticas');
        console.log('5. menu          - Mostrar este menú');
        console.log('6. exit          - Salir');
        console.log('🎮'.repeat(15).cyan);
    }

    function promptUser() {
        rl.question('\n🎯 Comando: '.green, (input) => {
            const command = input.trim().toLowerCase();
            
            switch (command) {
                case '1':
                case 'ping':
                    client.sendPing();
                    break;
                    
                case '2':
                case 'subscribe':
                    rl.question('📋 Canal a suscribir: ', (channel) => {
                        client.subscribeToChannel(channel);
                        promptUser();
                    });
                    return;
                    
                case '3':
                case 'history':
                    client.showNotificationHistory();
                    break;
                    
                case '4':
                case 'stats':
                    client.showStats();
                    break;
                    
                case '5':
                case 'menu':
                    showMenu();
                    break;
                    
                case '6':
                case 'exit':
                    console.log('👋 Cerrando cliente...'.yellow);
                    client.disconnect();
                    rl.close();
                    process.exit(0);
                    
                default:
                    console.log('❌ Comando no reconocido'.red);
            }
            
            promptUser();
        });
    }

    showMenu();
    promptUser();
}

// Función principal
function main() {
    console.log('🔔 ASSEMBLIES STORE - CLIENTE DE NOTIFICACIONES'.cyan.bold);
    console.log('📱 Recibiendo notificaciones en tiempo real...'.cyan);
    
    const userId = process.argv[2] || 'user123';
    const authToken = process.argv[3] || process.env.AUTH_TOKEN;
    
    if (!authToken) {
        console.log('⚠️  Advertencia: No se proporcionó token de autorización'.yellow);
        console.log('💡 Uso: node notification-client.js <USER_ID> <AUTH_TOKEN>'.yellow);
    }
    
    const client = new NotificationClient(userId, authToken);
    
    // Manejar cierre del proceso
    process.on('SIGINT', () => {
        console.log('\n👋 Cerrando cliente de notificaciones...'.yellow);
        client.disconnect();
        process.exit(0);
    });
    
    client.connect();
    
    // Iniciar modo interactivo después de un breve delay
    setTimeout(() => {
        if (client.isConnected) {
            startInteractiveMode(client);
        }
    }, 2000);
}

if (require.main === module) {
    main();
}

module.exports = NotificationClient;
