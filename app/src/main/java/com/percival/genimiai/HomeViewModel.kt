package com.percival.genimiai

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel: ViewModel() {

    private val _uiState: MutableStateFlow<HomeUiState> = MutableStateFlow(HomeUiState.Initial)
    val uiState = _uiState.asStateFlow()

    private lateinit var generativeModel: GenerativeModel

    init {
        val config = generationConfig {
            temperature = 0.70f
        }

        generativeModel = GenerativeModel(
            modelName = "gemini-pro-vision",
            apiKey = com.percival.genimiai.BuildConfig.API_KEY,
            generationConfig = config
        )
    }


    fun  questioning(userInput:String, selectedImages: List<Bitmap>){
        _uiState.value = HomeUiState.Loading
        val prompt = "Take a look at images, and then answer the following question: $userInput"
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val content = content {
                    for (bitmap in selectedImages){
                        image(bitmap)
                    }
                    text(prompt)
                }
                var output = ""
                generativeModel.generateContentStream(content).collect{
                    output += it.text
                    _uiState.value = HomeUiState.Success(output)
                }
            }catch (e: Exception){
                _uiState.value = HomeUiState.Error(e.localizedMessage ?: "Error in generating message")
            }
        }
    }
}


sealed interface HomeUiState{

    object Initial: HomeUiState
    object Loading: HomeUiState
    data class Success(
        val outputText: String
    ): HomeUiState
    data class Error(
        val error: String
    ): HomeUiState
}