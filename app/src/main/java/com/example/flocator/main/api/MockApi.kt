package com.example.flocator.main.api

import com.example.flocator.main.models.User
import com.yandex.mapkit.geometry.Point
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.function.Supplier
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

class MockApi {
    companion object {
        private const val SPEED = 0.00000056
        private val friends = mutableListOf(
            User(
                1,
                Point(59.985908, 30.348651),
                "https://sun9-49.userapi.com/impg/TdcpO6GlXJovrdcUevufkrI9W8J6iPdrhgefvw/rm9idTOQhDc.jpg?size=800x1200&quality=95&sign=1fdec8e7b9c628738d3b772f2a833456&type=album"
            ),
            User(
                2,
                Point(59.953061, 30.359045),
                "https://sun9-80.userapi.com/impg/zFqqZwH4dbMWOPw9JFCfA3Ka8nNXgdKtmu_QqA/sotUNpUze_M.jpg?size=577x842&quality=95&sign=83071863fec73f501facf658d104cca7&type=album"
            ),
            User(
                3,
                Point(59.928843, 30.360394),
                "https://sun9-16.userapi.com/impf/c834402/v834402625/8a9e0/mkem4S3O8a4.jpg?size=640x867&quality=96&sign=da195b0b134db8a4b043d721ca81da1a&type=album"
            )
        )
        private val listOfDirections = listOf(
            Supplier {
                Random.nextDouble(0.0, 1.0)
            },
            Supplier {
                Random.nextDouble(-1.0, 0.0)
            },
            Supplier {
                Random.nextDouble(0.0, 1.0)
            }
        )

        fun getAllFriends(): Single<MutableList<User>> {
            return Single.create {
                it.onSuccess(friends)
            }.subscribeOn(Schedulers.io())
        }

        fun watchFriends(): Observable<MutableList<User>> {
            return Observable.create {
                while (true) {
                    for (i in friends.indices) {
                        val friend = friends[i]
                        val pointTo = Point(
                            friend.point.latitude + listOfDirections[i].get(),
                            friend.point.longitude + listOfDirections[i].get(),
                        )
                        friend.point = moveWithSpeed(friend.point, pointTo, SPEED)
                        friends[i] = friend
                        it.onNext(friends)
                    }
                    Thread.sleep(16)
                }
            }.subscribeOn(Schedulers.io())
        }

        private fun moveWithSpeed(from: Point, to: Point, speed: Double): Point {
            val a = abs(from.latitude - to.latitude)
            val b = abs(from.longitude - to.longitude)
            if (sqrt(a * a + b * b) < speed) {
                return to
            }
            val c = b / a
            var latitude = sqrt(speed * speed / (c * c + 1))
            var longitude = latitude * c
            if (to.latitude < from.latitude) {
                latitude = -latitude
            }
            if (to.longitude < from.longitude) {
                longitude = -longitude
            }
            return Point(from.latitude + latitude, from.longitude + longitude)
        }
    }
}