package com.answersolutions.runandread

import com.answersolutions.runandread.data.model.AudioBookTextFrame
import com.answersolutions.runandread.data.model.TextPart
import com.answersolutions.runandread.ui.player.TextTimeRelationsTools
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    val parts = listOf(
        TextPart(
            startTimeMms = 0,
            text = "Сергей  Павлов.  « Лунная  радуга».  Книга  первая.  По  чёрному  следу.  «Где  мы ?»  —  спросил  человек.  «На  звёздной  дороге»,  —  ответил  звёздный  олень.  Сверкнув  рогами,  грациозно  выгнал  шею  и  посмотрел  вперёд.  «Пойдём ?»  —  «Конечно.  Другого  пути  у  нас  нет».  И  они  пошли.  Рядом.  Часть  первая.  К  вопросу  об  аллигаторах.  Росклоосвещённый  зал  и  неожиданно  заканчивалось  широким  раструбом,  довольно  высоко  над  полом.  Фрэнк  высунулся  из  трубы  по  пояс.  Он  сделал  попытку  хватиться  за  верхний  край  раструба.  Не  удалось.  Прыгать  вниз  головой  не  хотелось,  но  другого  выхода  не  было.  Фрэнк  вытер  пот  с  лица  испачканным  ржавчиной  рукавом,  привстал  на  руках  и,  рывком  подтянув"
        ),
        TextPart(
            startTimeMms = 80700,
            text = "ноги,  швырнул  себя  в  воздух.  Приземлился  он  сравнительно  мягко.  Кошкой.  Вскочил.  Внимательно  осмотрелся,  насколько  это  позволяло  тусклое  освещение.  Было  жарко  и  сыро,  и  где -то  шумела  вода.  Только  теперь  ему  пришло  в  голову,  что  здешние  лабиринты  очень  напоминают  нижние  ярусы  старой  венерианской  базы  «Маммут».  Побаливало  бедро.  Это  результат  поспешного  спуска  по  спиральному  желобу  во  время  пожара  в  кольцевой  галерее.  Впрочем,  легко  отделался.  Неизвестно,  чем  бы  всё  это  кончилось,  если  бы  не  заприметил  спасительный  желоб,  ещё  до  того,  как  сработали  огнетушители.  Фрэнк  с  омерзением  плюнул.  Создатели  пены  отнюдь  не  заботили  её  вкусовые  достоинства.  Поправил  подмышкой  кабру  с  бластером  и  пошёл  в"
        ),
        TextPart(
            startTimeMms = 127760,
            text = "обход  зала.  Не  считая  массивной  решётки,  запирающей  низкую  полуовальную  амбразуру  непонятного  назначения,  зал  был  пуст.  Фрэнк  вынул  нож,  включил  вмонтированный  в  рукоять  фонарик  и  направил  его  на  решётку.  Луч  упал  на  глянцевую  поверхность  воды.  Должно  быть  бассейн.  И,  наверное,  очень  большой,  потому  что  свет  фонаря  не  достигал  противоположной  стенки.  Над  водой  кудился  туман.  Фрэнк  бесполезно  подёргал  решётку.  Похоже,  зал  —  это  вовсе  не  зал,  а  просто  большая  цистерна.  Фрэнк  осмотрел  гладкие  стены  и  понял,  что  наверху  они  не  сливаются  с  потолком.  Потолочная  крышка  наверняка  приподнята  над  закрая  на  этого  металлического  стакана,  иначе  под  крышку  не  проникали  бы  отблески  внешних  светильников."
        ),
        TextPart(
            startTimeMms = 170880,
            text = "Вдоль  стены  свисала  тонкая  труба.  Конец  трубы  не  слишком  высоко,  и,  если  подключить,  он  мог  подпрыгнуть.  Выхватив  бластер,  Фрэнк  стремительно  обернулся.  Ему  почудилось  какое -то  движение  наверху,  с  тыла.  Минуту  он  сматривался  в  гребень  стены,  оружие  на  изготовку.  Вокруг  всё  было  спокойно.  Подозрение,  что  это,  может  быть,  выглядывал  дыроглаз,  мало -помалу  угасло.  Почудилось,  значит.  Фрэнк  спрятал  оружие  и,  немного  расслабившись  перед  прыжком,  направился  к  тонкой  трубе.  Лязгнул  металл.  Бзанк!  Фрэнк  потерял  под  ногами  опору  и,  падая  в  темноту,  он  инстинктивно  сжался,  защищая  руками  голову  от  удара.  Шумный  всплеск.  Вынырнув,  Фрэнк  перевёл  дыхание  и  бешено  взглянул  наверх.  В  зените  светлый  круг"
        ),
        TextPart(
            startTimeMms = 218800,
            text = "похожен  на  большую  тусклую  луну.  Растяпа.  Наивный  котёнок.  Надо  же,  люк  обойти  не  сумел.  Потом  он  решил,  что  растяпа  —  это,  пожалуй,  слишком.  Тем  более,  что  встретился  не  просто  люк.  Обыкновенный  люк  он,  конечно,  бы  заметил.  Это  что -нибудь  наподобие  входа  в  сливной  колодец,  закрытого  многолепестковой  диафрагмой.  Знакомые  штучки.  Когда  диафрагма  не  заперта,  по  ней  и  леса  не  пройдёт.  Он  поводил  рукой  в  темноте  и  нащупал  шершавую  стенку.  Ухватиться  здесь  было  не  за  что.  Откуда -то  струйка  мелилась  вода.  Малейший  всплеск  порождал  звучное  эхо.  Вода  имела  неприятный  привкус  металла.  На  фоне  тускло  светящейся  горловины  люка  появился  силуэт  округлого  выступа.  Впечатление"
        )
    )

    fun getCurrentText(elapsedMilliseconds: Double): AudioBookTextFrame {
        if (parts.isEmpty()) return AudioBookTextFrame("", 0, null, null)

        for (i in parts.indices.reversed()) {
            val part = parts[i]
            print("$i; ${part.startTimeMms}; ${part.text}")
            val nextPart = parts.getOrNull(i + 1)
            if (part.startTimeMms <= elapsedMilliseconds) {
                return AudioBookTextFrame(
                    text = part.text.replace("\n", ""),
                    startTimeMms = part.startTimeMms,
                    nextStartTime = nextPart?.startTimeMms,
                    nextText = nextPart?.text?.replace("\n", "")
                )
            }
        }
        return AudioBookTextFrame("", 0, null, null)
    }

    @Test
    fun addition_isCorrect() {
        val test = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9)

        var position = 25 * 1000
        repeat(10) {
            val result = getCurrentText(elapsedMilliseconds = position.toDouble())

            val title1 = TextTimeRelationsTools.getCurrentBookmarkText(
                elapsedSeconds = position.toDouble() / 1000,
                currentText = result.text,
                currentStartTime = result.startTimeMms,
                nextStartTime = result.nextStartTime ?: result.startTimeMms,
                nextText = result.nextText
            )
            print(title1)
            position += (10 * 1000)
        }


//        val title2 = TextTimeRelationsTools.getCurrentBookmarkText(
//            elapsedSeconds = 50.0,
//            currentText = currentText,
//            currentStartTime = currentStartTime,
//            nextStartTime = nextStartTime,
//            nextText = nextText
//        )
//
//        val title3 = TextTimeRelationsTools.getCurrentBookmarkText(
//            elapsedSeconds = 70.0,
//            currentText = currentText,
//            currentStartTime = currentStartTime,
//            nextStartTime = nextStartTime,
//            nextText = nextText
//        )


//        print(title1)
//        print(title2)
//        print(title3)
//        print(test.indices)
//        print(test.indices.reversed())
//        assertEquals(4, 2 + 2)
    }
}