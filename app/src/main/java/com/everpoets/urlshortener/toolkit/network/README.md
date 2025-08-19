# API Logging Interceptor

Este interceptor foi adicionado para capturar e logar todas as chamadas de API realizadas pela aplicação.

## Funcionalidades

### Logs de Request
- **Método HTTP**: GET, POST, PUT, DELETE, etc.
- **URL completa**: Incluindo query parameters
- **Headers**: Todos os headers da requisição
- **Body**: Conteúdo do body (limitado a 1KB para evitar logs excessivos)

### Logs de Response
- **Status Code**: 200, 404, 500, etc.
- **Tempo de resposta**: Duração em milissegundos
- **Headers**: Todos os headers da resposta
- **Body**: Conteúdo da resposta (limitado a 1KB)

### Tratamento de Conteúdo
- **Texto/JSON**: Conteúdo é logado na íntegra
- **Binário**: Apenas o tamanho é logado
- **Truncamento**: Conteúdos maiores que 1KB são truncados

## Tags de Log

- `ApiLogging`: Tag principal para todos os logs
- Níveis de log:
  - `DEBUG`: Detalhes completos (headers, bodies)
  - `INFO`: Resumo das chamadas e respostas
  - `ERROR`: Falhas de rede

## Exemplo de Logs

### Request
```
D/ApiLogging: 🚀 HTTP REQUEST
D/ApiLogging: Method: POST
D/ApiLogging: URL: https://url-shortener-server.onrender.com/api/alias
D/ApiLogging: Headers:
D/ApiLogging:   Content-Type: application/json
D/ApiLogging: Request Body (application/json):
D/ApiLogging: {"url":"https://example.com"}
D/ApiLogging: 🚀 END REQUEST
```

### Response
```
I/ApiLogging: 📥 HTTP RESPONSE
I/ApiLogging: URL: https://url-shortener-server.onrender.com/api/alias
I/ApiLogging: Status: 201 Created
I/ApiLogging: Duration: 245ms
I/ApiLogging: Response Body (application/json):
I/ApiLogging: {"alias":"abc123","_links":{"self":"https://example.com","short":"https://short.ly/abc123"}}
I/ApiLogging: ✅ POST https://url-shortener-server.onrender.com/api/alias -> 201 (245ms)
I/ApiLogging: 📥 END RESPONSE
```

## Configuração

O interceptor é automaticamente adicionado ao `OkHttpClient` no `ApiModule`:

```kotlin
@Provides
@Singleton
fun provideOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(ApiLoggingInterceptor())
        .callTimeout(TIMEOUT, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
}
```

## Filtragem de Logs

Para ver apenas os logs de API no Logcat:
```
adb logcat -s ApiLogging
```

Para ver logs de diferentes níveis:
```
adb logcat ApiLogging:D *:S  # Apenas DEBUG e acima
adb logcat ApiLogging:I *:S  # Apenas INFO e acima
```
