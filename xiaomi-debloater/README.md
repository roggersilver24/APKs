# Xiaomi Debloater — Android (fase 1)

App nativo Android para Xiaomi, Redmi e POCO, planejado para analisar apps preinstalados em MIUI e HyperOS, com interface em portugues.

## Implementado nesta fase

- Aplicativo Android Kotlin + Jetpack Compose independente do Amamenta Bebe.
- Scanner local de aplicativos instalados, busca e filtros por classificacao.
- Classificador conservador: protegido, candidato para revisao, desconhecido e aplicativo de usuario.
- Nenhuma remocao/desativacao ainda — primeiro verificaremos a lista de bloqueio, o estado de Shizuku e a restauracao.
- Suite de testes unitarios para classificacao de pacotes.
- Pipeline separado de build e APK debug para testes controlados.

## Compilar

Requer JDK 17 e Android SDK 35:

```sh
cd xiaomi-debloater
./gradlew testDebugUnitTest lintDebug assembleDebug
```

APK de teste: `app/build/outputs/apk/debug/app-debug.apk`. Nao e uma versao de limpeza pronta para uso.

## Planejado (nao implementado)

1. Integracao Shizuku com autorizacao explicita e UserService; sem root.
2. Lista de pacotes revisada por ROM/versao, regras e explicacoes; **sem prometer suporte a todo Xiaomi**.
3. Modo conservador de desativacao, transacao por pacote, diagnostico e restauracao.
4. Historico local e operacoes em lote somente para pacotes previamente verificados.

## Seguranca

O app nao envia a lista de aplicativos para servidores e nao executa comandos ADB nesta fase. Pacotes essenciais sao protegidos por regra; classificacao de candidato nao significa remocao segura. A permissao QUERY_ALL_PACKAGES e necessaria para a funcao central de inventario de aplicativos e deve ser revisada antes de eventual publicacao na Play Store.

Referencias de pesquisa: Xiaomi Debloater, HyperOS Debloater, Universal Android Debloater NG e documentacao oficial Shizuku.
