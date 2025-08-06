const WebSocket = require('ws');
const colors = require('colors');

class StockMonitor {
    constructor(authToken) {
        this.authToken = authToken;
        this.ws = null;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 3000;
    }

    connect() {
        const wsUrl = 'ws://localhost:8081/api/ws/stock';
        
        console.log('📊 Conectando al monitor de stock...'.yellow);
        
        this.ws = new WebSocket(wsUrl, {
            headers: this.authToken ? {
                'Authorization': `Bearer ${this.authToken}`
            } : {}
        });

        this.ws.on('open', () => {
            console.log('✅ Monitor de stock conectado!'.green);
            this.reconnectAttempts = 0;
            this.subscribeToStockUpdates();
        });

        this.ws.on('message', (data) => {
            try {
                const message = JSON.parse(data.toString());
                this.handleStockMessage(message);
            } catch (error) {
                console.log('📨 Mensaje raw:', data.toString());
            }
        });

        this.ws.on('close', (code, reason) => {
            console.log(`❌ Conexión cerrada: ${code} - ${reason}`.red);
            this.attemptReconnect();
        });

        this.ws.on('error', (error) => {
            console.log('🚫 Error:', error.message.red);
        });
    }

    handleStockMessage(message) {
        const timestamp = new Date().toLocaleString();
        
        switch (message.type) {
            case 'STOCK_UPDATE':
                this.displayStockUpdate(timestamp, message);
                break;
                
            case 'OUT_OF_STOCK':
                this.displayOutOfStock(timestamp, message);
                break;
                
            case 'WELCOME':
                console.log(`🎉 [${timestamp}] ${message.message}`.green);
                break;
                
            default:
                console.log(`📨 [${timestamp}] Mensaje:`.cyan, JSON.stringify(message, null, 2));
        }
    }

    displayStockUpdate(timestamp, message) {
        console.log('\n' + '='.repeat(60).blue);
        console.log(`📊 ACTUALIZACIÓN DE STOCK - ${timestamp}`.blue.bold);
        console.log('='.repeat(60).blue);
        
        if (message.data) {
            console.log(`🏷️  Producto: ${message.data.productName || message.data.productId}`.white);
            console.log(`📦 Stock Anterior: ${message.data.previousStock}`.yellow);
            console.log(`📈 Stock Actual: ${message.data.currentStock}`.green);
            
            const change = message.data.currentStock - message.data.previousStock;
            const changeColor = change > 0 ? 'green' : 'red';
            const changeSymbol = change > 0 ? '↗️' : '↘️';
            
            console.log(`${changeSymbol} Cambio: ${change}`[changeColor]);
            console.log(`🔄 Tipo: ${message.data.changeType || 'UNKNOWN'}`.cyan);
        }
        
        // Alertas basadas en nivel de stock
        if (message.data && message.data.currentStock <= 5) {
            console.log('⚠️  ALERTA: Stock bajo (≤ 5 unidades)'.red.bold);
        } else if (message.data && message.data.currentStock <= 10) {
            console.log('⚡ AVISO: Stock moderado (≤ 10 unidades)'.yellow.bold);
        }
        
        console.log('='.repeat(60).blue + '\n');
    }

    displayOutOfStock(timestamp, message) {
        console.log('\n' + '⚠️'.repeat(20).red);
        console.log(`🚫 PRODUCTO AGOTADO - ${timestamp}`.red.bold);
        console.log('⚠️'.repeat(20).red);
        
        if (message.data) {
            console.log(`🏷️  Producto: ${message.data.productName}`.white);
            console.log(`🆔 ID: ${message.data.productId}`.gray);
            console.log(`📦 Stock: ${message.data.currentStock || 0}`.red);
        }
        
        console.log('🔔 Se requiere reabastecimiento inmediato!'.red.bold);
        console.log('⚠️'.repeat(20).red + '\n');
    }

    subscribeToStockUpdates() {
        const subscribeMessage = {
            type: 'SUBSCRIBE',
            channel: 'stock_updates'
        };
        
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(subscribeMessage));
            console.log('🔔 Suscrito a actualizaciones de stock'.green);
        }
    }

    attemptReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`🔄 Intentando reconexión ${this.reconnectAttempts}/${this.maxReconnectAttempts} en ${this.reconnectDelay/1000}s...`.yellow);
            
            setTimeout(() => {
                this.connect();
            }, this.reconnectDelay);
        } else {
            console.log('❌ Máximo número de intentos de reconexión alcanzado'.red);
        }
    }

    disconnect() {
        if (this.ws) {
            this.ws.close();
        }
    }

    // Simular cambios de stock para pruebas
    simulateStockChanges() {
        console.log('🧪 Modo de simulación activado'.magenta);
        
        const products = [
            { id: 'PROD001', name: 'iPhone 15 Pro' },
            { id: 'PROD002', name: 'Samsung Galaxy S24' },
            { id: 'PROD003', name: 'MacBook Air M3' }
        ];
        
        setInterval(() => {
            const product = products[Math.floor(Math.random() * products.length)];
            const changeType = Math.random() > 0.5 ? 'SALE' : 'RESTOCK';
            const change = changeType === 'SALE' ? -Math.floor(Math.random() * 3 + 1) : Math.floor(Math.random() * 10 + 1);
            const currentStock = Math.max(0, Math.floor(Math.random() * 50));
            const previousStock = currentStock - change;
            
            const message = {
                type: 'STOCK_UPDATE',
                data: {
                    productId: product.id,
                    productName: product.name,
                    previousStock: previousStock,
                    currentStock: currentStock,
                    changeType: changeType
                }
            };
            
            this.handleStockMessage(message);
        }, 5000);
    }
}

// Función principal
function main() {
    console.log('📊 ASSEMBLIES STORE - MONITOR DE STOCK'.blue.bold);
    console.log('🔍 Monitoreando cambios de inventario...'.cyan);
    
    // Obtener token de autorización de argumentos o variable de entorno
    const authToken = process.argv[2] || process.env.AUTH_TOKEN;
    
    if (!authToken) {
        console.log('⚠️  Advertencia: No se proporcionó token de autorización'.yellow);
        console.log('💡 Uso: node stock-monitor.js <AUTH_TOKEN>'.yellow);
        console.log('💡 O establece la variable de entorno AUTH_TOKEN'.yellow);
    }
    
    const monitor = new StockMonitor(authToken);
    
    // Manejar cierre del proceso
    process.on('SIGINT', () => {
        console.log('\n👋 Cerrando monitor de stock...'.yellow);
        monitor.disconnect();
        process.exit(0);
    });
    
    // Opción para modo simulación
    if (process.argv.includes('--simulate')) {
        monitor.simulateStockChanges();
    } else {
        monitor.connect();
    }
}

if (require.main === module) {
    main();
}

module.exports = StockMonitor;
