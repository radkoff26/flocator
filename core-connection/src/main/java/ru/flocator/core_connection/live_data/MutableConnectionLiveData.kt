package ru.flocator.core_connection.live_data

class MutableConnectionLiveData : ConnectionLiveData() {
    public override fun setValue(value: Boolean) {
        super.setValue(value)
    }

    public override fun postValue(value: Boolean) {
        super.postValue(value)
    }
}