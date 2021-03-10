package com.udacity.project4.shared

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

private data class Landmark( var title: String?,
                             var description: String?,
                             var location: String?,
                             var latitude: Double?,
                             var longitude: Double)

class FakeDataUsingLondonLandmarks {

    companion object {
        private var list = mutableListOf<Landmark>()

        fun generateShuffledData() {
            with(list) {

                clear() // to avoid duplicates, if not empty - from previous data - clear the list

                // add the 15 London landmarks to the list of fake data
                add(
                    Landmark(
                        title = "Big Ben",
                        description = "16-storey Gothic clocktower and national symbol at the Eastern end of the Houses of Parliament.",
                        location = "London", 51.5007292, -0.1268141
                    )
                )

                add(
                    Landmark(
                        title = "Buckingham Palace",
                        description = "Visitors can tour the palace's opulent private and state rooms or watch the changing of the guard.",
                        location = "London", 51.501364, -0.1440787
                    )
                )

                add(
                    Landmark(
                        title = "Westminster Abbey",
                        description = "",
                        location = "London", 51.4994245, -0.1297526
                    )
                )

                add(
                    Landmark(
                        title = "Madame Tussauds",
                        description = "Museum chain for life-size wax replicas of famous celebrities & historic icons in themed galleries.",
                        location = "London", 51.5230174, -0.15655
                    )
                )

                add(
                    Landmark(
                        title = "Tower Bridge",
                        description = "Panoramic views from high level walkways and behind-the-scenes access to original lifting machinery.",
                        location = "London", 51.5054564, -0.0775452
                    )
                )

                add(
                    Landmark(
                        title = "Tower of London",
                        description = "Centuries of bloody history around a medieval castle, home to Crown Jewels and iconic Beefeaters.",
                        location = "London", 51.5081124, -0.078138
                    )
                )

                add(
                    Landmark(
                        title = "Hyde Park",
                        description = "Huge green space, home to Diana Memorial Fountain, with boating and swimming in the Serpentine lake.",
                        location = "London", 51.5072682, -0.167919
                    )
                )

                add(
                    Landmark(
                        title = "The Shard",
                        description = "87-floor glass skyscraper with a jagged peak, with restaurants, offices, hotel and viewing platform.",
                        location = "London", 51.5045, -0.0886887
                    )
                )

                add(
                    Landmark(
                        title = "St. Paul's Cathedral",
                        description = "Churchyard and gardens outside Saint Paul's cathedral, with a floor-plan of the original building.",
                        location = "London", 51.5138453, -0.1005393
                    )
                )

                add(
                    Landmark(
                        title = "Tate Modern",
                        description = "Modern-art gallery with international works on display, plus a cafe with panoramic river views.",
                        location = "London", 51.5074691, -0.1020116
                    )
                )

                add(
                    Landmark(
                        title = "Royal Albert Hall",
                        description = "One of Britain's great Victorian splendours, most famous for the Proms classical music festival.",
                        location = "London", 51.5009088, -0.1795547
                    )
                )

                add(
                    Landmark(
                        title = "Trafalgar Square",
                        description = "Nelson's Column rises above this iconic square's LED-lit fountains, artworks and lion statues.",
                        location = "London", 51.50809, -0.1291379
                    )
                )

                add(
                    Landmark(
                        title = "Kensington Palace",
                        description = "Royal palace and gardens, with exhibits on former residents like Queen Victoria and Princess Diana.",
                        location = "London", 51.5038918, -0.1936707
                    )
                )

                add(
                    Landmark(
                        title = "Science Museum",
                        description = "A vast treasury of science and invention, famous for its state-of-the-art interactive exhibits.",
                        location = "London", 51.4978095, -0.1832782
                    )
                )

                add(
                    Landmark(
                        title = "The British Museum",
                        description = "Huge showcase for global antiquities, including Egyptian mummies and ancient Greek sculptures.",
                        location = "London", 51.5194133, -0.1291453
                    )
                )

                shuffle()
            }
        }

        fun getItemsCount() = list.count()

        fun getNextDTOItem(): ReminderDTO {
            if (getItemsCount() == 0)
                generateShuffledData()

            val item = list.removeAt(0)
            return ReminderDTO(
                title = item.title,
                description = item.description,
                location = item.location,
                latitude = item.latitude,
                longitude = item.longitude
            )
        }

        fun getNextDataItem(): ReminderDataItem {
            if (getItemsCount() == 0)
                generateShuffledData()

            val item = list.removeAt(0)
            return ReminderDataItem(
                title = item.title,
                description = item.description,
                location = item.location,
                latitude = item.latitude,
                longitude = item.longitude
            )
        }
    }
}