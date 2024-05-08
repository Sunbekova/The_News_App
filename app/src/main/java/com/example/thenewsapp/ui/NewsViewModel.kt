package com.example.thenewsapp.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.thenewsapp.models.Article
import com.example.thenewsapp.models.NewsResponse
import com.example.thenewsapp.repository.NewsRepository
import com.example.thenewsapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import java.util.Locale.IsoCountryCode

class NewsViewModel(app:Application,val  NewsRepository: NewsRepository): AndroidViewModel(app){
    val headlines: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var headlinesPage = 1
    var headlinesResponce: NewsResponse? = null

    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponse: NewsResponse? = null
    var newSearchQuery: String? = null
    var oldSearchQuery:String? = null

    init{
        getHeadlines("us")
    }

    fun getHeadlines(countryCode: String) = viewModelScope.launch {
        headlinesInternet(countryCode)
    }

    fun searchNews(searchQuery: String) = viewModelScope.launch {
        searchNewsInternet(searchQuery)
    }
    private fun handleHeadlinesResponse(response: Response<NewsResponse>): Resource<NewsResponse>{
        if(response.isSuccessful){
            response.body()?.let{resultResponce ->
                headlinesPage++
                if(headlinesResponce == null){
                    headlinesResponce = resultResponce
                }else{
                    val oldArticles = headlinesResponce?.articles
                    val newArticles = resultResponce.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(headlinesResponce?:resultResponce)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleSearchNewsResponce(responce: Response<NewsResponse>) : Resource<NewsResponse> {
        if(responce.isSuccessful){
            responce.body()?.let { resultResponce ->
                if(searchNewsResponse == null || newSearchQuery != oldSearchQuery){
                    searchNewsPage = 1
                    oldSearchQuery = newSearchQuery
                    searchNewsResponse = resultResponce
                }else{
                    searchNewsPage++
                    val oldArticles = searchNewsResponse?.articles
                    val newArticles = resultResponce.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(searchNewsResponse ?: resultResponce)
            }
        }
        return Resource.Error(responce.message())
    }
    fun addToFavourites(article: Article) = viewModelScope.launch {
        NewsRepository.upsert(article)
    }

    fun getFavouriteNews() = NewsRepository.getFavouriteNews()

    fun deleteArticle(article: Article) = viewModelScope.launch {
        NewsRepository.upsert(article)
    }

    fun internetConnection(context: Context): Boolean{
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).apply{
            return getNetworkCapabilities(activeNetwork)?.run {
                when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            } ?: false
        }
    }
    private suspend fun headlinesInternet(countryCode: String){
        headlines.postValue(Resource.Loading())
        try{
            if(internetConnection(this.getApplication())){
                val response = NewsRepository.getHeadlines(countryCode, headlinesPage)
                headlines.postValue((handleHeadlinesResponse(response)))
            }
            else{
                headlines.postValue(Resource.Error("No internet connection!"))
            }
        }
        catch(t: Throwable){
            when(t){
                is IOException -> headlines.postValue(Resource.Error("Unable to connect"))
                else -> headlines.postValue(Resource.Error("No signal!"))
            }
        }
    }
    private suspend fun searchNewsInternet(searchQuery: String){
        newSearchQuery = searchQuery
        searchNews.postValue(Resource.Loading())
        try{
            if(internetConnection(this.getApplication())){
                val response = NewsRepository.SearchNews(searchQuery, searchNewsPage)
                searchNews.postValue(handleSearchNewsResponce(response))
            }
            else {
                searchNews.postValue(Resource.Error("No internet connection!"))
            }
        } catch(t: Throwable){
            when(t){
                is IOException -> searchNews.postValue(Resource.Error("Unable to connect!"))
                else -> searchNews.postValue(Resource.Error("No signal"))
            }
        }
    }
}