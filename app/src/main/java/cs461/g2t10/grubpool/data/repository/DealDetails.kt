package cs461.g2t10.grubpool.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cs461.g2t10.grubpool.data.api.DbInterface
import cs461.g2t10.grubpool.data.models.FoodDeal
import cs461.g2t10.grubpool.repository.NetworkState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers.io

class DealDetails(
    private val apiService: DbInterface, private val compositeDisposable: CompositeDisposable
) {
    private val _networkState = MutableLiveData<NetworkState>()
    val networkState: LiveData<NetworkState>
        get() = _networkState

    private val _getDealsByStore = MutableLiveData<List<FoodDeal>>()
    val getDealsByStore: LiveData<List<FoodDeal>>
        get() = _getDealsByStore

    fun fetchDealDetails(userId: String) {
        _networkState.postValue(NetworkState.LOADING)

        try {
            compositeDisposable.add(apiService.getDeals(userId).subscribeOn(io()).subscribe({
                _getDealsByStore.postValue(it)
                _networkState.postValue(NetworkState.LOADED)
            }, {
                _networkState.postValue(NetworkState.ERROR)
                Log.e("DealDetails", " ${it.message}")
            }))

        } catch (e: java.lang.Exception) {
            System.err.println("[data:repository:DealDetails] An error occurred while fetching deal details: $e")
        }


    }

}