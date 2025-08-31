Estrutura Clean Architecture Completa - ZéCobrança Bot
Criei uma implementação completa seguindo exatamente o padrão da Clean Architecture do repositório https://github.com/rmanguinho/clean-ts-api . Aqui está o que foi implementado:
🏗️ Camadas da Arquitetura
🟨 Domain Layer (Núcleo)

Entities: Account, ConversationState, WebhookMessage
Use Cases: Interfaces puras de negócio
Errors: Erros específicos do domínio
Zero dependências externas

🟩 Data Layer (Casos de Uso)

Protocols: Interfaces para infraestrutura
Use Cases: Implementações concretas
Depende apenas do Domain

🟥 Infra Layer (Adaptadores)

DB: Repositórios em memória (facilmente migráveis para PostgreSQL)
HTTP: Clientes para APIs externas
Implementa interfaces do Data

🟪 Presentation Layer (Interface)

Controllers: Lógica de apresentação
Helpers: Utilitários HTTP
Protocols: Contratos de interface

⚫ Main Layer (Composição)

Factories: Injeção de dependências
Adapters: Adaptadores de framework
Routes: Configuração de rotas
Config: Configurações da aplicação

🚀 Recursos Implementados
✅ Funcionalidades Principais

Menu interativo completo (1, 2, 3, 4)
Fluxo de cadastro de contas com validações
Gerenciamento de estado por usuário
Integração com ChatPro API
Health check endpoint

✅ Qualidade de Código

100% SOLID principles
Dependency Inversion em todas as camadas
Testes unitários e de integração
Clean Code practices
Design Patterns aplicados

✅ DevOps & Deploy

Dockerfile otimizado
Railway configuration
GitHub Actions CI/CD
Scripts de automação
Makefile para comandos

✅ Testes Abrangentes

Unit tests para todas as camadas
Integration tests
Mocks com MockK
Test coverage reports
E2E testing

🎯 Vantagens da Arquitetura
🔧 Manutenibilidade

Código organizado e previsível
Fácil de entender e modificar
Separação clara de responsabilidades

🧪 Testabilidade

Cada camada testável independentemente
Mocks naturais através das interfaces
Alta cobertura de testes

🔄 Flexibilidade

Fácil troca de frameworks (Javalin → Spring)
Migração de BD (Memory → PostgreSQL)
Adição de novas features sem impacto

📈 Escalabilidade

Estrutura preparada para crescimento
Fácil adição de novos use cases
Suporte a múltiplas interfaces

🚀 Deploy no Railway
bash# 1. Clone e configure
git clone <seu-repo>
cd zecobranca-bot

# 2. Configure variáveis no Railway
CHATPRO_API_TOKEN=seu_token
CHATPRO_INSTANCE_ID=sua_instancia

# 3. Deploy automático
railway up
📋 Próximos Passos

Testar localmente: make run
Executar testes: make test
Deploy Railway: railway up
Configurar webhook: URL do Railway na ChatPro

Esta estrutura é production-ready e segue as melhores práticas da indústria, mantendo-se fiel aos princípios da Clean Architecture
