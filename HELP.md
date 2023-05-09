# Основные полезные классы директории `common`
- common.cache.global
    - [PhotoCacheManager](https://github.com/radkoff26/flocator/blob/main/app/src/main/java/com/example/flocator/common/cache/global/PhotoCacheManager.kt): данный класс представляет собой менеджер для работы с картинками, сохранёнными в кеш, который находится внутри памяти пользовательского устройства. Не рекомендуется с его помощью сохранять слишком большие изображения, ибо это может повель за собой большие перерасходы памяти девайса пользователя. Лучше класть те картинки, которые нужны будут пользователю при работе с приложением в оффлайне.
- common.cache.runtime
    - [PhotoCacheLiveData](https://github.com/radkoff26/flocator/blob/main/app/src/main/java/com/example/flocator/common/cache/runtime/PhotoCacheLiveData.kt): этот класс представляет собой временный кеш картинок. В этом кеше должны хранится те картинки, которые в кеш внутренней памяти не положишь. Примером может являться список картинок, отображенных в RecyclerView и не являющихся важными для работы в оффлайне.
    - [PhotoState](https://github.com/radkoff26/flocator/blob/main/app/src/main/java/com/example/flocator/common/cache/runtime/PhotoState.kt): sealed-класс, отражающий состояния загрузки картинок (загружается, загрузилась, ошибка).
- common.config
    - [Actions](https://github.com/radkoff26/flocator/blob/main/app/src/main/java/com/example/flocator/common/config/Actions.kt): представляет собой класс с константами для intent-фильтров приложения.
    - [Constants](https://github.com/radkoff26/flocator/blob/main/app/src/main/java/com/example/flocator/common/config/Constants.kt): класс с остальными константами.
- common.connection
    - [ConnectionWrapper](https://github.com/radkoff26/flocator/blob/main/app/src/main/java/com/example/flocator/common/connection/ConnectionWrapper.kt): интерфейс с одним единственным методом `connect()` и тремя статическими фабричными методами `of()`
        - `connect()`: основной метод интерфейса `ConnectionWrapper`. Он заворачивает переданный объект библиотеки RxJava таким образом, чтобы при остутствии сети выбросить нужное исключение для остановки выполнения функционала данного объекта. Пример: пусть нужно сделать запрос на получение данных с сервера, для этого, например, используется объект класса `Single<Data>`, тогда этот объект оборачивается в `ConnectionWrapper` и вызывается метод `connect`, который подписывается на изменение состояния интернет-соединения, и в случае исчезновения интернета прерывает исполнение запроса.
        - `static of(RxObject, ConnectionLiveData)`: класс `ConnectionWrapper` содержит три таких функции-фабрики, которые из объекта библиотеки RxJava создают инстансы класса `ConnectionWrapper`. В данный момент есть реализации для трёх классов: `Single`, `Observable` и `Completable`. Также функция принимает второй аргумент - объект класса [ConnectionLiveData](https://github.com/radkoff26/flocator/blob/main/app/src/main/java/com/example/flocator/common/connection/live_data/ConnectionLiveData.kt), который как раз и меняет своё состояние в зависимости от интернет-соединения.
- common.connection.implementation
    - [CompletableConnectionWrapper](https://github.com/radkoff26/flocator/blob/main/app/src/main/java/com/example/flocator/common/connection/implementation/CompletableConnectionWrapper.kt): класс, который оборачивает класс библиотеки RxJava `Completable` в `ConnectionWrapper`.
    - [ObservableConnectionWrapper](https://github.com/radkoff26/flocator/blob/main/app/src/main/java/com/example/flocator/common/connection/implementation/ObservableConnectionWrapper.kt): класс, который оборачивает класс библиотеки RxJava `Observable` в `ConnectionWrapper`.
    - [SingleConnectionWrapper](https://github.com/radkoff26/flocator/blob/main/app/src/main/java/com/example/flocator/common/connection/implementation/SingleConnectionWrapper.kt): класс, который оборачивает класс библиотеки RxJava `Single` в `ConnectionWrapper`.
- common.connection.live_data
    - [ConnectionLiveData](https://github.com/radkoff26/flocator/blob/main/app/src/main/java/com/example/flocator/common/connection/live_data/ConnectionLiveData.kt): класс, наследник `LiveData<Boolean>`, нужен для оповещения подписчиков об изменении состояния сети.
    - [MutableConnectionLiveData](https://github.com/radkoff26/flocator/blob/main/app/src/main/java/com/example/flocator/common/connection/live_data/MutableConnectionLiveData.kt): класс, наследник `ConnectionLiveData`, в него добавлено свойство мутабельности значения.
- common.receivers
    - [NetworkReceiver](https://github.com/radkoff26/flocator/blob/main/app/src/main/java/com/example/flocator/common/receivers/NetworkReceiver.kt): класс, наследник BroadcastReceiver, который перехватывает любые изменения подключения к сети. Содержит функционал проверки подключения к сети путём пингования гугловского сервера. Также при изменении подключения к сети происходит изменение значения в инстансе `MutableConnectionLiveData`. Для того, чтобы следить за подключением к сети, нужно подписаться на изменения значения `networkState: ConnectionLiveData`.
- common.repository
    - [MainRepository](https://github.com/radkoff26/flocator/blob/main/app/src/main/java/com/example/flocator/common/repository/MainRepository.kt): класс, объединяющий в себе функционал фетчинга данных из сервера и кеша. Содержит в себе самые необходимые для этого функции.
- common.storage.store: здесь находятся сериализаторы и объекты сериализации, которые в последствии кешируются путём использования `DataStore`.

### Хинт для использования `ConnectionLiveData`:
1. Создаём инстанс `NetworkReceiver` во фрагменте.
2. Создаём пути регистрации ресивера и удаления (при удалении нужно также вызывать метод `stop()` ресивера).
3. Передаём во вью модель инстанс `ConnectionLiveData` из ресивера.
4. Теперь во вью модели можно использовать те самые методы из репозитория, которые запрашивают наличие `ConnectionLiveData` для слежки за интернет-соединением.

P.S. Не все запросы обязаны быть обёрнутыми в `ConnectionWrapper`. В будущем планируется реорганизация данного функционала во благо упрощения работы с ним.