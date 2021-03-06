package ru.mpei.feature_profile.mvi

import android.provider.ContactsContract
import kekmech.ru.common_mvi.BaseReducer
import kekmech.ru.common_mvi.Result
import ru.mpei.domain_profile.dto.ParamsItem
import ru.mpei.domain_profile.dto.ProfileItem
import ru.mpei.feature_profile.mvi.ProfileEvent.News
import ru.mpei.feature_profile.mvi.ProfileEvent.Wish

typealias ProfileResult = Result<ProfileState, ProfileEffect, ProfileAction>

class ProfileReducer : BaseReducer<ProfileState, ProfileEvent, ProfileEffect, ProfileAction>{
    override fun reduce(event: ProfileEvent, state: ProfileState): ProfileResult  = when (event) {
        is Wish -> processWish(event, state)
        is News -> processItems(event, state)
    }

    private fun processItems(event: News, state: ProfileState): ProfileResult = when (event) {

        is News.Authorized -> Result(
            state = state.copy(
                isLoading = false,
                profileData = event.profile,
                isAuthorized = true
            )
        )

        is News.AuthorizationFailed -> Result(
            state = ProfileState(),
            effect = ProfileEffect.AuthorizationError(event.throwable)
        )

        is News.Authenticated -> Result(
            state = state.copy(
                isLoading = true,
                paramsItem = event.paramsItem
            ),
            action = ProfileAction.Authorize(event.paramsItem.id, event.paramsItem.pass),
            effect = ProfileEffect.SaveParams(event.paramsItem)
        )

        is News.AuthenticationFailed -> Result(
            state = ProfileState(),
            effect = ProfileEffect.AuthenticationError(event.throwable)
        )

        is News.TasksLoaded -> Result(
            state = state.copy(
                isLoading = false,
                tasksList = event.tasksList
            )
        )

        is News.TasksLoadFailed -> Result(
            state = state.copy(
                isLoading = false
            ),
            effect = ProfileEffect.TasksLoadError(event.throwable)
        )

    }

    private fun processWish(event: Wish, state: ProfileState): ProfileResult = when (event) {

        is Wish.System.InitLogin -> Result(
            state = ProfileState()
        )

        is Wish.Authorization -> Result(
            state = state.copy(
                isLoading = true,
                paramsItem = ParamsItem(id = event.id, pass = event.pass)
            ),
            action = ProfileAction.Authorize(event.id, event.pass)
        )

        is Wish.ValidateFields -> Result(
            state = state.copy(
                paramsItem = ParamsItem(email = event.email, pass = event.pass)
            ),
            effect = ProfileEffect.Validate(email = event.email, pass = event.pass)
        )

        is Wish.ValidationFailed -> Result(
            state = ProfileState()
        )

        is Wish.Authentication -> Result(
            state = state.copy(
                isLoading = true
            ),
            action = ProfileAction.Authenticate(state.paramsItem.email, state.paramsItem.pass)
        )

        is Wish.Exit -> Result(
            state = ProfileState(),
            effect = ProfileEffect.ClearParams
        )

        is Wish.LoadTasks -> Result(
            state = state.copy(
                isLoading = true
            ),
            action = ProfileAction.LoadTasks(event.type, state.profileData.id.toString())
        )
    }
}