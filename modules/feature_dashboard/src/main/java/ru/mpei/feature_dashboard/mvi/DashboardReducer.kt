package ru.mpei.feature_dashboard.mvi

/**
 * Андрей Турлюк
 * А-08-17
 */
import kekmech.ru.common_mvi.BaseReducer
import kekmech.ru.common_mvi.Result
import ru.mpei.feature_dashboard.mvi.DashboardEvent.News
import ru.mpei.feature_dashboard.mvi.DashboardEvent.Wish

typealias DashboardResult = Result<DashboardState, DashboardEffect, DashboardAction>

// Обработчик намерений и событий
class DashboardReducer : BaseReducer<DashboardState, DashboardEvent, DashboardEffect, DashboardAction> {

    override fun reduce(event: DashboardEvent, state: DashboardState): DashboardResult = when (event) {
        is Wish -> processWish(event, state)
        is News -> processItems(event, state)
    }

    // Обработк событий
    private fun processItems(event: News, state: DashboardState): DashboardResult = when (event) {
        is News.NewsListLoaded -> Result(
            state = state.copy(
                isLoading = false,
                newsList = event.listOfItems
            ),
            effect = DashboardEffect.NewsListLoaded
        )
        is News.NewsListLoadError -> Result(
            state = state.copy(isLoading = false),
            effect = DashboardEffect.ShowError(event.throwable)
        )

        is News.EventsListLoaded -> Result(
            state = state.copy(
                isLoading = false,
                eventsList = event.listOfItems
            ),
            effect = DashboardEffect.EventsListLoaded
        )
        is News.EventsListLoadError -> Result(
            state = state.copy(isLoading = false),
            effect = DashboardEffect.ShowError(event.throwable)
        )
    }

    // Обработка намерений
    private fun processWish(event: Wish, state: DashboardState): DashboardResult = when (event) {
        is Wish.System.Init -> Result(
            state = state.copy(isLoading = true),
            effects = emptyList(),
            actions = listOf(
                DashboardAction.LoadNewsList,
                DashboardAction.LoadEventsList
            )
        )
        is Wish.GetEvents -> Result(
            state = state.copy(isLoading = true),
            action = DashboardAction.LoadEventsList
        )
        is Wish.GetNews -> Result(
            state = state.copy(isLoading = true),
            action = DashboardAction.LoadNewsList
        )
        is Wish.OnPageChange -> Result(
            state = state.copy(
                selectedPage = event.position
            )
        )
    }
}