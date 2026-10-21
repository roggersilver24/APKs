# Migracao para central de APKs

- Aplicacao Amamenta Bebe movida da raiz para `amamenta-bebe/`, mantendo os mesmos arquivos (blobs Git).
- Pipeline Actions mantido na raiz em `.github/workflows/build-apk.yml`, alterado para usar `amamenta-bebe/` como diretorio de trabalho.
- Firebase, backend e wrapper Gradle acompanham o aplicativo para preservar caminhos relativos.
- `applicationId`, esquema Room, versao Android, assinatura e segredos nao foram alterados.
- Xiaomi Debloater sera independente em `xiaomi-debloater/`.

## Verificacao antes de integrar

1. Rodar workflow no pull request; confirmar testes Firestore e `testDebugUnitTest lintDebug lintRelease assembleRelease`.
2. Conferir configuracao de Firebase que utilize caminhos locais e automacoes externas; atualizar diretorio-base para `amamenta-bebe/` quando aplicavel.
3. Certificar que o APK de distribuicao continua bloqueado sem assinatura original verificada.
4. Renomear o repositorio para `APKs` somente depois de revisar integracoes e clones.

## Volta segura

A branch `main` continua intacta ate eventual aprovacao/merge do PR. E possivel fechar o PR sem afetar o projeto publicado.
