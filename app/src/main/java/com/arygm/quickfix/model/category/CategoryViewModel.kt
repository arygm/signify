package com.arygm.quickfix.model.category

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

open class CategoryViewModel(private val categoryRepositoryFirestore: CategoryRepositoryFirestore) :
    ViewModel() {

  private val _categories = MutableStateFlow<List<Category>>(emptyList())
  val categories: StateFlow<List<Category>> = _categories

  private val _subcategories = MutableStateFlow<List<Subcategory>>(emptyList())
  val subcategories: StateFlow<List<Subcategory>> = _subcategories

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CategoryViewModel(CategoryRepositoryFirestore(Firebase.firestore)) as T
          }
        }
  }

  init {
    categoryRepositoryFirestore.init {
      Log.d("CategoryViewModel", "CategoryRepositoryFirestore initialized")
      getCategories()
    }
  }

  fun getCategories() {
    categoryRepositoryFirestore.fetchCategories(
        onSuccess = { categories -> _categories.value = categories as List<Category> },
        onFailure = { e ->
          Log.e("CategoryViewModel", "getCategories has not been able to fetch all the categories")
        })
  }

  fun getSubcategories(categoryId: String) {
    categoryRepositoryFirestore.fetchSubcategories(
        categoryId = categoryId,
        onSuccess = { subcategories -> _subcategories.value = subcategories as List<Subcategory> },
        onFailure = { e ->
          Log.e("CategoryViewModel", "Error fetching subcategories for $categoryId", e)
        })
  }

  fun getCategoryBySubcategoryId(subcategoryId: String, onSuccess: (Category?) -> Unit) {
    categoryRepositoryFirestore.fetchCategoryBySubcategoryId(
        subcategoryId = subcategoryId,
        onSuccess = { category -> onSuccess(category) },
        onFailure = { e ->
          Log.e("CategoryViewModel", "Error fetching category for subcategory ID $subcategoryId", e)
        })
  }
}
