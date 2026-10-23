# APKs — Central de aplicativos Android

Este repositório reúne projetos Android independentes, cada um com seus próprios arquivos de compilação.

| Aplicativo | Diretório | Situação |
| --- | --- | --- |
| **Amamenta Bebê** | [amamenta-bebe/](amamenta-bebe/) | Projeto preservado |
| **Xiaomi Debloater** | [xiaomi-debloater/](xiaomi-debloater/) | **Pausado por solicitação do responsável** |

## Estrutura

```text
APKs/
├── amamenta-bebe/       # Código Android, backend, Firebase, testes e Gradle
├── xiaomi-debloater/     # Código Android e testes — desenvolvimento pausado
├── .github/workflows/   # Compilações independentes de cada aplicativo
└── README.md
```

### Amamenta Bebê

Abra a pasta [amamenta-bebe](amamenta-bebe/) para manutenção. O `applicationId` original permanece `br.com.amamentabebe`, e a proteção de assinatura de APK continua em vigor. **Não distribua um novo APK sobre uma instalação com dados reais sem conferir a assinatura original.**

### Xiaomi Debloater

Arquivado **como projeto pausado**, não como repositório GitHub arquivado. O código e os testes foram preservados em [xiaomi-debloater](xiaomi-debloater/). **Não desenvolver nem publicar novas versões até nova solicitação.** A versão experimental não foi validada em aparelho físico.

### Compilações

- `.github/workflows/build-apk.yml`: Amamenta Bebê.
- `.github/workflows/build-xiaomi.yml`: Xiaomi Debloater.

Os projetos não compartilham módulos nem configurações Gradle.

A organização foi realizada a partir do repositório antigo `amamenta-bebe`. A **mudança do nome remoto para `APKs`** é uma configuração administrativa do GitHub, independente desta organização de arquivos. Aponte clones locais para a URL nova depois de renomear.
