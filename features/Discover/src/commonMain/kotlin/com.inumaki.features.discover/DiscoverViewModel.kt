package com.inumaki.features.discover

import com.inumaki.core.ui.model.Title
import com.inumaki.core.ui.model.PosterData
import com.inumaki.core.ui.model.ViewModel
import com.inumaki.features.discover.model.DiscoverList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface DiscoverUiState {
    object Loading : DiscoverUiState
    data class Success(val items: List<DiscoverList>) : DiscoverUiState
    data class Error(val message: String) : DiscoverUiState
}

data class DiscoverItem(
    val id: String,
    val title: String
)

class DiscoverViewModel : ViewModel() {

    private val _state = MutableStateFlow<DiscoverUiState>(DiscoverUiState.Loading)
    val state: StateFlow<DiscoverUiState> = _state

    init {
        load()
    }

    private fun load() {
        scope.launch {
            delay(2000)

            val carouselData = DiscoverList(
                title = "",
                list = listOf(
                    PosterData(
                        title = Title(
                            primary = "Frieren: Beyond \nJourney’s End Season 2",
                            secondary = "葬送のフリーレン 第2期"
                        ),
                        poster = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx182255-butzrqd4I0aC.jpg",
                        description = "The second season of Frieren continues the emotional fantasy journey in Winter 2026.",
                        indicator = "Airing"
                    ),
                    PosterData(
                        title = Title(
                            primary = "Jujutsu Kaisen: Shimetsu Kaiyuu - Zenpen",
                            secondary = "呪術廻戦 死滅回游 前編"
                        ),
                        poster = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx172463-YRX9lR2ggWo8.jpg",
                        description = "Jujutsu Kaisen Season 3 Culling Game arc Part 1, airing in Winter 2026.",
                        indicator = "Airing"
                    ),
                    PosterData(
                        title = Title(
                            primary = "Jigokuraku 2nd Season",
                            secondary = "地獄楽 第2期"
                        ),
                        poster = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx179541-GXoU1sP3Z2qF.jpg",
                        description = "The dark fantasy series Hell’s Paradise continues in Winter 2026.",
                        indicator = "Airing"
                    ),
                    PosterData(
                        title = Title(
                            primary = "[Oshi no Ko] 3rd Season",
                            secondary = "[推しの子] 第3期"
                        ),
                        poster = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx182587-Q5KXF3WqyxuD.jpg",
                        description = "The third season of Oshi no Ko’s dramatic story, Winter 2026.",
                        indicator = "Airing"
                    ),
                    PosterData(
                        title = Title(
                            primary = "Medalist 2nd Season",
                            secondary = "メダリスト 第2期"
                        ),
                        poster = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx189275-uE6LlWow4EMf.jpg",
                        description = "Figure skating drama returns for Season 2 in Winter 2026.",
                        indicator = "Airing"
                    )
                )
            )

            val trendingNow = DiscoverList(
                title = "Trending Now",
                list = listOf(
                    PosterData(
                        title = Title(
                            primary = "JUJUTSU KAISEN Season 3: The Culling Game Part 1",
                            secondary = "呪術廻戦 死滅回游 前編"
                        ),
                        poster = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx172463-LnXqHzt74SJL.jpg",
                        indicator = "${4 - 1}/12",
                        description = "The third season of Jujutsu Kaisen. After the Shibuya Incident, a deadly jujutsu battle known as the Culling Game orchestrated by Noritoshi Kamo erupts across ten colonies in Japan. Haunted by guilt over the mass killings in Shibuya and wary of Sukuna’s interest in Fushiguro, Itadori chooses not to return to Jujutsu High. Instead, he teams up with Choso to exorcise the countless cursed spirits unleashed by Noritoshi Kamo. Amid this chaos, the Jujutsu Headquarters revokes Yuji Itadori’s suspended death sentence and appoints special-grade sorcerer Yuta Okkotsu as his executioner. Newly awakened modern sorcerers and resurrected ancient ones, now fighting as players in the Culling Game, collide with conflicting motives, driving the world once more toward an age ruled by jujutsu."
                    ),
                    PosterData(
                        title = Title(
                            primary = "Fire Force Season 3 Part 2",
                            secondary = "炎炎ノ消防隊 参ノ章 第2クール"
                        ),
                        poster = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx179062-pbzYE1miZq61.png",
                        indicator = "${3 - 1}/13",
                        description = "The second cour of Enen no Shouboutai: San no Shou. As chaos spreads, Shinra and Company 8 fight to stop a final, world-ending blaze."
                    ),
                    PosterData(
                        title = Title(
                            primary = "ことなかれヒーロー じんじゃーまん",
                            secondary = "ことなかれヒーロー じんじゃーまん"
                        ),
                        poster = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/b180822-OkqBdkzrFFpi.png",
                        indicator = "12/12",
                        description = "Shorts about root vegetables from the refrigerator who try to act like superheroes."
                    ),
                    PosterData(
                        title = Title(
                            primary = "Frieren: Beyond Journey’s End Season 2",
                            secondary = "葬送のフリーレン 第2期"
                        ),
                        poster = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx182255-butzrqd4I0aC.jpg",
                        indicator = "${2 - 1}/10",
                        description = "The second season of Sousou no Frieren."
                    ),
                    PosterData(
                        title = Title(
                            primary = "Medalist Season 2",
                            secondary = "メダリスト 第2期"
                        ),
                        poster = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx189275-eHsK0lNnFfXH.jpg",
                        indicator = "${1 - 1}/0",
                        description = "The second season of Medalist."
                    ),
                    PosterData(
                        title = Title(
                            primary = "My Hero Academia: Vigilantes Season 2",
                            secondary = "ヴィジランテ -僕のヒーローアカデミア ILLEGALS- 第2期"
                        ),
                        poster = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx195322-hqBf19mBtM5K.jpg",
                        indicator = "${3 - 1}/13",
                        description = "The second season of Vigilante: Boku no Hero Academia ILLEGALS."
                    )
                )
            )



            val upcomingShows = DiscoverList(
                title = "Upcoming Next Season",
                list = listOf(
                    PosterData(
                        title = Title("Ghost in the Shell", "GHOST IN THE SHELL / 攻殻機動隊"),
                        poster = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx43-Y6EjeEMM14dj.png",
                        description = "Upcoming",
                        indicator = "1/1"
                    ),
                    PosterData(
                        title = Title("Re:ZERO -Starting Life in Another World-", "Re:ゼロから始める異世界生活"),
                        poster = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx21355-wRVUrGxpvIQQ.jpg",
                        description = "Upcoming",
                        indicator = "1/25"
                    ),
                    PosterData(
                        title = Title("Black Clover", "ブラッククローバー"),
                        poster = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx97940-fyh8o7gNbha0.png",
                        description = "Upcoming",
                        indicator = "1/170"
                    ),
                    PosterData(
                        title = Title("Golden Kamuy", "ゴールデンカムイ"),
                        poster = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx99699-mBCjpoWpAVGX.jpg",
                        description = "Upcoming",
                        indicator = "1/12"
                    ),
                    PosterData(
                        title = Title("Mushoku Tensei: Jobless Reincarnation", "無職転生 ～異世界行ったら本気だす～"),
                        poster = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx108465-1ANspF1EWyFx.jpg",
                        description = "Upcoming",
                        indicator = "1/11"
                    ),
                    PosterData(
                        title = Title("BLEACH: Thousand-Year Blood War", "BLEACH 千年血戦篇"),
                        poster = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx116674-p3zK4PUX2Aag.jpg",
                        description = "Upcoming",
                        indicator = "1/13"
                    ),
                    PosterData(
                        title = Title("MF GHOST", "MFゴースト"),
                        poster = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx143327-6gv1QzquNg2o.png",
                        description = "Upcoming",
                        indicator = "1/12"
                    ),
                    PosterData(
                        title = Title("Witch Hat Atelier", "とんがり帽子のアトリエ"),
                        poster = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx147105-fNMGNpV19G47.jpg",
                        description = "Upcoming",
                        indicator = "1/?"
                    ),
                    PosterData(
                        title = Title("Daemons of the Shadow Realm", "黄泉のツガイ"),
                        poster = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx195600-KfuPU2Xhy5kG.png",
                        description = "Upcoming",
                        indicator = "1/?"
                    )
                )
            )


            val listData = listOf(
                carouselData,
                trendingNow,
                upcomingShows
            )

            _state.value = DiscoverUiState.Success(listData)
        }
    }
}
