package gary.interview.itunes.di

import gary.interview.itunes.api.MusicDataApi
import gary.interview.itunes.ui.main.MainViewModel
import org.koin.androidx.viewmodel.experimental.builder.viewModel
import org.koin.dsl.module.module

val appModule = module {

    single { MusicDataApi() }

    viewModel<MainViewModel>()
}