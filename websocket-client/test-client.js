const WebSocket = require('ws');
const colors = require('colors');

class WebSocketTester {
    constructor() {
        this.connections = {};
        this.testResults = [];
        this.startTime = Date.now();
    }

    async runAllTests() {
        console.log('🧪 INICIANDO SUITE DE PRUEBAS WEBSOCKET'.rainbow.bold);
        console.log('=' * 60);
        
        try {
            await this.testGeneralEndpoint();
            await this.testNotificationsEndpoint();
            await this.testStockEndpoint();
            await this.testMessageHandling();
            await this.testReconnection();
            
            this.showResults();
        } catch (error) {
            console.log('❌ Error durante las pruebas:', error.message.red);
        }
    }

    async testGeneralEndpoint() {
        return new Promise((resolve, reject) => {
            console.log('\n📡 PRUEBA 1: Endpoint General'.blue.bold);
            console.log('-'.repeat(40).blue);
            
            const startTime = Date.now();
            const ws = new WebSocket('ws://localhost:8081/api/ws/general');
            
            let welcomed = false;
            
            const timeout = setTimeout(() => {
                this.recordTest('General Connection', false, 'Timeout');
                ws.close();
                reject(new Error('Timeout en conexión general'));
            }, 5000);
            
            ws.on('open', () => {
                console.log('✅ Conectado al endpoint general'.green);
                
                // Enviar ping
                ws.send(JSON.stringify({ type: 'PING' }));
            });
            
            ws.on('message', (data) => {
                try {
                    const message = JSON.parse(data.toString());
                    
                    if (message.type === 'WELCOME') {
                        welcomed = true;
                        console.log(`🎉 Mensaje de bienvenida recibido: ${message.message}`.green);
                    }
                    
                    if (message.type === 'PONG') {
                        console.log('🏓 PONG recibido correctamente'.green);
                        
                        clearTimeout(timeout);
                        const duration = Date.now() - startTime;
                        this.recordTest('General Connection', true, `${duration}ms`);
                        
                        ws.close();
                        resolve();
                    }
                    
                } catch (error) {
                    console.log('📨 Mensaje no JSON:', data.toString().gray);
                }
            });
            
            ws.on('error', (error) => {
                clearTimeout(timeout);
                this.recordTest('General Connection', false, error.message);
                reject(error);
            });
        });
    }

    async testNotificationsEndpoint() {
        return new Promise((resolve, reject) => {
            console.log('\n🔔 PRUEBA 2: Endpoint Notificaciones'.yellow.bold);
            console.log('-'.repeat(40).yellow);
            
            const startTime = Date.now();
            const ws = new WebSocket('ws://localhost:8081/api/ws/notifications');
            
            const timeout = setTimeout(() => {
                this.recordTest('Notifications Connection', false, 'Timeout');
                ws.close();
                reject(new Error('Timeout en conexión notificaciones'));
            }, 5000);
            
            ws.on('open', () => {
                console.log('✅ Conectado al endpoint de notificaciones'.green);
                
                // Suscribirse a un canal
                ws.send(JSON.stringify({
                    type: 'SUBSCRIBE',
                    channel: 'test_notifications'
                }));
            });
            
            ws.on('message', (data) => {
                try {
                    const message = JSON.parse(data.toString());
                    
                    if (message.type === 'SUBSCRIPTION_CONFIRMED') {
                        console.log(`✅ Suscripción confirmada: ${message.channel}`.green);
                        
                        clearTimeout(timeout);
                        const duration = Date.now() - startTime;
                        this.recordTest('Notifications Connection', true, `${duration}ms`);
                        
                        ws.close();
                        resolve();
                    }
                    
                } catch (error) {
                    console.log('📨 Mensaje no JSON:', data.toString().gray);
                }
            });
            
            ws.on('error', (error) => {
                clearTimeout(timeout);
                this.recordTest('Notifications Connection', false, error.message);
                reject(error);
            });
        });
    }

    async testStockEndpoint() {
        return new Promise((resolve, reject) => {
            console.log('\n📊 PRUEBA 3: Endpoint Stock (sin autenticación)'.red.bold);
            console.log('-'.repeat(40).red);
            
            const startTime = Date.now();
            const ws = new WebSocket('ws://localhost:8081/api/ws/stock');
            
            const timeout = setTimeout(() => {
                // Para stock esperamos que falle sin auth
                this.recordTest('Stock Connection (no auth)', true, 'Correctly rejected');
                resolve();
            }, 3000);
            
            ws.on('open', () => {
                console.log('⚠️  Conexión abierta sin autenticación (inesperado)'.yellow);
                clearTimeout(timeout);
                this.recordTest('Stock Connection (no auth)', false, 'Should require auth');
                ws.close();
                resolve();
            });
            
            ws.on('close', (code, reason) => {
                console.log(`✅ Conexión rechazada correctamente: ${code} - ${reason}`.green);
                clearTimeout(timeout);
                this.recordTest('Stock Connection (no auth)', true, `Rejected: ${code}`);
                resolve();
            });
            
            ws.on('error', (error) => {
                console.log(`✅ Error esperado: ${error.message}`.green);
                clearTimeout(timeout);
                this.recordTest('Stock Connection (no auth)', true, 'Auth required');
                resolve();
            });
        });
    }

    async testMessageHandling() {
        return new Promise((resolve, reject) => {
            console.log('\n💬 PRUEBA 4: Manejo de Mensajes'.cyan.bold);
            console.log('-'.repeat(40).cyan);
            
            const ws = new WebSocket('ws://localhost:8081/api/ws/general');
            const messagesReceived = [];
            
            const timeout = setTimeout(() => {
                const success = messagesReceived.length >= 2;
                this.recordTest('Message Handling', success, `${messagesReceived.length} messages`);
                ws.close();
                success ? resolve() : reject(new Error('Insufficient messages'));
            }, 5000);
            
            ws.on('open', () => {
                console.log('✅ Conectado para prueba de mensajes'.green);
                
                // Enviar diferentes tipos de mensajes
                setTimeout(() => ws.send(JSON.stringify({ type: 'PING' })), 500);
                setTimeout(() => ws.send('Mensaje de texto plano'), 1000);
                setTimeout(() => ws.send(JSON.stringify({
                    type: 'SUBSCRIBE',
                    channel: 'test_channel'
                })), 1500);
            });
            
            ws.on('message', (data) => {
                messagesReceived.push(data.toString());
                console.log(`📨 Mensaje ${messagesReceived.length} recibido`.cyan);
                
                try {
                    const message = JSON.parse(data.toString());
                    console.log(`   Tipo: ${message.type}`.gray);
                } catch (e) {
                    console.log(`   Texto plano: ${data.toString().substring(0, 50)}...`.gray);
                }
            });
            
            ws.on('error', (error) => {
                clearTimeout(timeout);
                this.recordTest('Message Handling', false, error.message);
                reject(error);
            });
        });
    }

    async testReconnection() {
        return new Promise((resolve, reject) => {
            console.log('\n🔄 PRUEBA 5: Reconexión'.magenta.bold);
            console.log('-'.repeat(40).magenta);
            
            let reconnected = false;
            let connectionCount = 0;
            
            const connect = () => {
                connectionCount++;
                const ws = new WebSocket('ws://localhost:8081/api/ws/general');
                
                ws.on('open', () => {
                    console.log(`✅ Conexión ${connectionCount} establecida`.green);
                    
                    if (connectionCount === 1) {
                        // Cerrar la primera conexión después de un breve momento
                        setTimeout(() => {
                            console.log('🔌 Cerrando primera conexión...'.yellow);
                            ws.close();
                        }, 1000);
                    } else {
                        // Segunda conexión exitosa
                        reconnected = true;
                        this.recordTest('Reconnection', true, 'Successful');
                        ws.close();
                        resolve();
                    }
                });
                
                ws.on('close', () => {
                    if (connectionCount === 1 && !reconnected) {
                        console.log('🔄 Intentando reconexión...'.yellow);
                        setTimeout(connect, 1000);
                    }
                });
                
                ws.on('error', (error) => {
                    this.recordTest('Reconnection', false, error.message);
                    reject(error);
                });
            };
            
            connect();
            
            // Timeout de seguridad
            setTimeout(() => {
                if (!reconnected) {
                    this.recordTest('Reconnection', false, 'Timeout');
                    reject(new Error('Timeout en reconexión'));
                }
            }, 10000);
        });
    }

    recordTest(testName, success, details) {
        this.testResults.push({
            name: testName,
            success: success,
            details: details,
            timestamp: new Date().toLocaleTimeString()
        });
    }

    showResults() {
        const totalTime = Date.now() - this.startTime;
        const successCount = this.testResults.filter(t => t.success).length;
        const totalTests = this.testResults.length;
        
        console.log('\n' + '🎯'.repeat(20).green);
        console.log('🎯 RESULTADOS DE PRUEBAS'.green.bold);
        console.log('🎯'.repeat(20).green);
        
        this.testResults.forEach((test, index) => {
            const status = test.success ? '✅ PASS'.green : '❌ FAIL'.red;
            console.log(`${index + 1}. ${test.name} - ${status}`);
            console.log(`   Detalles: ${test.details}`.gray);
            console.log(`   Tiempo: ${test.timestamp}`.gray);
            console.log();
        });
        
        console.log('📊 RESUMEN:'.blue.bold);
        console.log(`   ✅ Exitosas: ${successCount}/${totalTests}`.green);
        console.log(`   ❌ Fallidas: ${totalTests - successCount}/${totalTests}`.red);
        console.log(`   ⏱️  Tiempo total: ${(totalTime / 1000).toFixed(2)}s`.yellow);
        
        const successRate = (successCount / totalTests * 100).toFixed(1);
        console.log(`   📈 Tasa de éxito: ${successRate}%`.cyan);
        
        console.log('\n🎯'.repeat(20).green);
    }

    // Prueba de carga básica
    async loadTest(connections = 10) {
        console.log(`\n⚡ PRUEBA DE CARGA: ${connections} conexiones`.yellow.bold);
        console.log('-'.repeat(50).yellow);
        
        const promises = [];
        
        for (let i = 0; i < connections; i++) {
            promises.push(new Promise((resolve) => {
                const ws = new WebSocket('ws://localhost:8081/api/ws/general');
                const startTime = Date.now();
                
                ws.on('open', () => {
                    const connectTime = Date.now() - startTime;
                    ws.send(JSON.stringify({ type: 'PING' }));
                    
                    setTimeout(() => {
                        ws.close();
                        resolve({ success: true, connectTime });
                    }, 100);
                });
                
                ws.on('error', () => {
                    resolve({ success: false, connectTime: -1 });
                });
                
                setTimeout(() => {
                    resolve({ success: false, connectTime: -1 });
                }, 5000);
            }));
        }
        
        const results = await Promise.all(promises);
        const successful = results.filter(r => r.success).length;
        const avgConnectTime = results
            .filter(r => r.success)
            .reduce((sum, r) => sum + r.connectTime, 0) / successful;
        
        console.log(`✅ Conexiones exitosas: ${successful}/${connections}`.green);
        console.log(`⏱️  Tiempo promedio de conexión: ${avgConnectTime.toFixed(2)}ms`.yellow);
        
        this.recordTest('Load Test', successful === connections, 
            `${successful}/${connections} connections, avg ${avgConnectTime.toFixed(2)}ms`);
    }
}

// Función principal
async function main() {
    console.log('🧪 ASSEMBLIES STORE - PROBADOR WEBSOCKET'.rainbow.bold);
    console.log('🔬 Ejecutando suite completa de pruebas...'.cyan);
    
    const tester = new WebSocketTester();
    
    try {
        if (process.argv.includes('--load')) {
            const connections = parseInt(process.argv[process.argv.indexOf('--load') + 1]) || 10;
            await tester.loadTest(connections);
        } else {
            await tester.runAllTests();
        }
        
        console.log('🎉 Suite de pruebas completada!'.green.bold);
        
    } catch (error) {
        console.log('💥 Error en suite de pruebas:'.red, error.message);
        process.exit(1);
    }
}

if (require.main === module) {
    main();
}

module.exports = WebSocketTester;
