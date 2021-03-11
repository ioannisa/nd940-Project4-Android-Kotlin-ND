package com.udacity.project4.shared

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

private data class ReminderForLandmark(var title: String?,
                                       var description: String?,
                                       var location: String?,
                                       var latitude: Double?,
                                       var longitude: Double)

class FakeDataUsingLondonLandmarks {

    companion object {
        private var list = mutableListOf<ReminderForLandmark>()

        private fun generateShuffledData() {
            with(list) {

                clear() // to avoid duplicates, if not empty - from previous data - clear the list

                // add the 15 London landmarks to the list of fake data
                add(
                    ReminderForLandmark(
                        title = "Take a pic under the clock",
                        description = "16-storey Gothic clocktower and national symbol at the Eastern end of the Houses of Parliament.",
                        location = "Big Ben", 51.5007292, -0.1268141
                    )
                )

                add(
                    ReminderForLandmark(
                        title = "Watch the guard change",
                        description = "Visitors can tour the palace's opulent private and state rooms or watch the changing of the guard.",
                        location = "Buckingham Palace", 51.501364, -0.1440787
                    )
                )

                add(
                    ReminderForLandmark(
                        title = "take a pic of the Abbey",
                        description = "",
                        location = "Westminster Abbey", 51.4994245, -0.1297526
                    )
                )

                add(
                    ReminderForLandmark(
                        title = "visit the museum",
                        description = "Museum chain for life-size wax replicas of famous celebrities & historic icons in themed galleries.",
                        location = "Madame Tussauds", 51.5230174, -0.15655
                    )
                )

                add(
                    ReminderForLandmark(
                        title = "take a pic of Thames river",
                        description = "Panoramic views from high level walkways and behind-the-scenes access to original lifting machinery.",
                        location = "Tower Bridge", 51.5054564, -0.0775452
                    )
                )

                add(
                    ReminderForLandmark(
                        title = "visit the crown jewels",
                        description = "Centuries of bloody history around a medieval castle, home to Crown Jewels and iconic Beefeaters.",
                        location = "Tower of London", 51.5081124, -0.078138
                    )
                )

                add(
                    ReminderForLandmark(
                        title = "visit Winter Wonderland",
                        description = "Huge green space, home to Diana Memorial Fountain, with boating and swimming in the Serpentine lake.",
                        location = "Hyde Park", 51.5072682, -0.167919
                    )
                )

                add(
                    ReminderForLandmark(
                        title = "Eat at Oblix Restaurant",
                        description = "87-floor glass skyscraper with a jagged peak, with restaurants, offices, hotel and viewing platform.",
                        location = "The Shard", 51.5045, -0.0886887
                    )
                )

                add(
                    ReminderForLandmark(
                        title = "take a pic of Cathedral",
                        description = "Churchyard and gardens outside Saint Paul's cathedral, with a floor-plan of the original building.",
                        location = "St. Paul's Cathedral", 51.5138453, -0.1005393
                    )
                )

                add(
                    ReminderForLandmark(
                        title = "visit modern art",
                        description = "Modern-art gallery with international works on display, plus a cafe with panoramic river views.",
                        location = "Tate Modern", 51.5074691, -0.1020116
                    )
                )

                add(
                    ReminderForLandmark(
                        title = "listen to the royal orchestra",
                        description = "One of Britain's great Victorian splendours, most famous for the Proms classical music festival.",
                        location = "Royal Albert Hall", 51.5009088, -0.1795547
                    )
                )

                add(
                    ReminderForLandmark(
                        title = "Take pic of Nelson's column and lions",
                        description = "Nelson's Column rises above this iconic square's LED-lit fountains, artworks and lion statues.",
                        location = "Trafalgar Square", 51.50809, -0.1291379
                    )
                )

                add(
                    ReminderForLandmark(
                        title = "Take a pic of Kesington Palace",
                        description = "Royal palace and gardens, with exhibits on former residents like Queen Victoria and Princess Diana.",
                        location = "Kensington Palace", 51.5038918, -0.1936707
                    )
                )

                add(
                    ReminderForLandmark(
                        title = "See science wonders",
                        description = "A vast treasury of science and invention, famous for its state-of-the-art interactive exhibits.",
                        location = "Science Museum", 51.4978095, -0.1832782
                    )
                )

                add(
                    ReminderForLandmark(
                        title = "See the Greek marbles",
                        description = "Huge showcase for global antiquities, including Egyptian mummies and ancient Greek sculptures.",
                        location = "The British Museum", 51.5194133, -0.1291453
                    )
                )

                shuffle()
            }
        }

        val nextDTOItem: ReminderDTO
            get()  {
                if (list.isEmpty())
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

        val nextDataItem: ReminderDataItem
            get(){
                if (list.isEmpty())
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