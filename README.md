# Central de APKs

Repositorio com projetos Android independentes.

| Projeto | Pasta | Situacao |
| --- | --- | --- |
| Amamenta Bebe | [amamenta-bebe](amamenta-bebe/) | Aplicativo existente, preservado |
| Xiaomi Debloater | [xiaomi-debloater](xiaomi-debloater/) | Aplicativo em desenvolvimento |

## Amamenta Bebe

Use `cd amamenta-bebe` para compilar com Gradle. O identificador Android, historico do banco, configurações e assinatura permanecem inalterados. O workflow `.github/workflows/build-apk.yml` usa esta subpasta. Consulte [as instrucoes originais](amamenta-bebe/README.md).

**Nao instale uma nova compilacao sobre um aparelho com dados reais sem conferir a assinatura da versao instalada.** A chave original nao esta no repositorio.

## Xiaomi Debloater

Veja [o projeto Xiaomi](xiaomi-debloater/README.md). Este app sera independente e nao tera acesso aos dados do Amamenta Bebe.

## Organizacao e migracao

Consulte [APKS-ORGANIZACAO.md](APKS-ORGANIZACAO.md). O nome remoto do repositorio ainda e `amamenta-bebe`; mudar seu nome requer verificar integracoes GitHub, Firebase, links e clones locais.
