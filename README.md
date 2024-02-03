### Project hieararchy
```
src
└── main
    └── kotlin
        ├── bot
        │   ├── AccountingChatBot.kt
        │   └── MessageScheduler.kt
        ├── excel
        │   ├── parser
        │   │   ├── ExcelParser.kt
        │   │   └── ScheduleParser.kt
        │   ├── utils
        │   │   ├── ScheduleBuilderFactory.kt
        │   │   ├── ScheduleFile.kt
        │   │   └── TimeObject.kt
        │   └── ExcelDataProccessor.kt
        ├── storage
        │   └── exposed
        │       ├── entities
        │       │   ├── ScheduleEntity.kt
        │       │   └── UserEntity.kt
        │       ├── models
        │       │   ├── BaseModel.kt
        │       │   ├── Schedule.kt
        │       │   └── User.kt
        │       ├── repository
        │       │   ├── impl
        │       │   │   ├── ScheduleRepositoryImpl.kt
        │       │   │   └── UserRepositoryImpl.kt
        │       │   ├── CrudRepository.kt
        │       │   ├── ScheduleRepository.kt
        │       │   └── UserRepository.kt
        │       ├── tables
        │       │   ├── ScheduleTable.kt
        │       │   └── UserTable.kt
        │       └── utils
        │           └── DatabaseSingleton.kt
        └── Main.kt
```

### Running
```
git clone git@github.com:fqrmix/accounting-chat-manager-kotlin.git &&\
cd accounting-chat-manager-kotlin &&\
docker compose up -d
```
