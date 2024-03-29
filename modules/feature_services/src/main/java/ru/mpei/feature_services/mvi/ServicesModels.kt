package ru.mpei.feature_services.mvi

/**
 * Андрей Турлюк
 * А-08-17
 */

import kekmech.ru.common_mvi.Feature

typealias ServicesFeature = Feature<ServicesState, ServicesEvent, ServicesEffect>

// Состяние фичи
data class ServicesState(
    val isLoading: Boolean = false,
    val listOfNews: List<Any> = emptyList()
)

sealed class ServicesEvent{

    sealed class Wish : ServicesEvent(){
        object System {
            object Init: Wish()
        }
    }

    sealed class Services : ServicesEvent() {

    }
}

sealed class ServicesEffect{
    data class ShowError(val throwable: Throwable): ServicesEffect()
}

sealed class ServicesAction {

}