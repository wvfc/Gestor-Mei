# Gestor MEI

Aplicativo Android nativo para administrar **duas empresas MEI** de forma simples,
sem login, funcionando localmente no celular.

## Funcionalidades

- Cadastro e administração de **duas (ou mais) empresas MEI** separadamente, com
  alternância rápida entre elas.
- **Dashboard inicial** com faturamento do mês/ano, limite anual do MEI, valor
  restante, percentual usado, compras do mês/ano, próximas reuniões e projetos
  em andamento.
- **Receitas**: cadastro, edição, exclusão e filtro por mês/ano.
- **Despesas / Compras**: cadastro, edição, exclusão e filtro por mês/ano.
- **Controle do limite MEI** (padrão R$ 81.000,00, editável por empresa) com
  cálculo de percentual usado, valor restante, média mensal, projeção até
  dezembro e **alertas visuais em 70%, 85% e 95%**.
- **Agenda** de reuniões e compromissos com status.
- **Projetos** com status e prioridade.
- **Acessos**: cofre simples de senhas (com mostrar/ocultar) e registro de
  e-mails usados em sites e serviços.
- **Ideias, melhorias e sugestões**.
- **Relatórios** resumidos por empresa.

## Tecnologias

- Kotlin + Jetpack Compose (Material 3)
- Room Database (SQLite local)
- Arquitetura MVVM + Repository Pattern
- Navegação por menu inferior (Navigation Compose)

## Estrutura

```
app/src/main/java/com/gestormei/
├── data/
│   ├── database/   # AppDatabase (Room)
│   ├── dao/        # Data Access Objects
│   ├── model/      # Entidades
│   └── repository/ # Repositórios
├── viewmodel/      # ViewModels (MVVM)
├── ui/
│   ├── screens/    # Telas
│   ├── components/ # Componentes reutilizáveis
│   └── theme/      # Tema (cores, tipografia)
└── util/           # Formatadores e utilitários
```

## Build

O projeto usa o Gradle Wrapper. Para gerar o APK de debug:

```bash
./gradlew assembleDebug
```

O APK é gerado em `app/build/outputs/apk/debug/app-debug.apk`.

## GitHub Actions

O workflow `.github/workflows/android-build.yml` compila o projeto e publica o
APK de debug como *artifact* a cada `push`, `release` ou execução manual.

## Próximas versões (não implementado nesta versão)

Login, backup automático, checklist, integração com nuvem, pagamento,
assinatura, multiusuário, sincronização online e criptografia do cofre de senhas
(a estrutura já está preparada para evoluir).
