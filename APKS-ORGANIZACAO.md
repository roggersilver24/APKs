# Organização do repositório APKs

## Estrutura
- `amamenta-bebe/`: aplicativo existente, incluindo Android, backend, Firebase, Gradle, testes e documentação.
- `xiaomi-debloater/`: projeto experimental **pausado**, com arquivos preservados.
- `.github/workflows/`: pipelines separados por aplicativo.
- `README.md`: índice dos projetos.

## Preservação
O código do Amamenta Bebê foi movido usando os mesmos blobs Git, sem alterar `applicationId`, dados locais, assinatura, banco Room, serviços, versão, dependências ou conteúdo dos arquivos. Os caminhos do workflow foram atualizados para `amamenta-bebe/`.

A compilação automática do Amamenta Bebê mantém verificação do certificado e bloqueio de distribuição sem a chave de assinatura original. O funcionamento em telefone real e integrações externas ainda requerem conferência quando houver manutenção.

## Alteração do nome do repositório
A renomeação do repositório para `APKs` deve ser feita em **Settings → General → Repository name**, uma vez que a conexão disponível para GitHub não oferece uma operação para alterar o nome remoto. Depois de renomear, use `https://github.com/roggersilver24/APKs` e atualize eventuais clones, integrações externas e links configurados manualmente.

## Limites
- Não modificar ou continuar o Xiaomi Debloater até nova solicitação.
- Evitar misturar versões, builds e configuração de assinatura entre aplicativos.
- Não alterar arquivos no celular nem migrar dados de produção como parte da organização do repositório.
