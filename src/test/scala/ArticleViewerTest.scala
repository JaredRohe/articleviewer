import articleviewer.{Article, ArticleViewer, ArticlesResponse, PageState}
import com.softwaremill.sttp._
import org.scalatest.FunSuite

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class ArticleViewerTest extends FunSuite {

    val av = new ArticleViewer()
    val MaxDuration = 60 seconds

    test("ArticleViewer.makeResponse"){

        val response = av.makeReqeust[ArticlesResponse](
            uri"https://api.elevio-staging.com/v1/articles")

        Await.result(response, MaxDuration)

        assert(response.value.get.isSuccess)

    }

    test("ArticleViewer.getNumPages") {

        val NUMPAGES = 5

        val numPagesFuture = av.getNumPages()

        Await.result(numPagesFuture, MaxDuration)

        assertResult(NUMPAGES)(numPagesFuture.value.get.get)

    }

    test("ArticleViewer.getArticlesForPage") {

        val PAGESTATE = PageState(1, List[Article](), false)
        val EXPECTED_PAGES = Set[String]("Introducing our in-app help", "Need a hand?", "Surfing is fun")

        val articlesFuture = av.getArticlesForPage(PAGESTATE)

        Await.result(articlesFuture, MaxDuration)

        var articlesSet = Set[String]()

        for(article <- articlesFuture.value.get.get){
            articlesSet += article.title

        }

        assert(EXPECTED_PAGES.diff(articlesSet).isEmpty)

    }

    test("ArticleViewer.getArticleDetails") {

        val ARTICLEID = 1

        val detailsFuture = av.getArticleDetails(ARTICLEID)

        Await.result(detailsFuture, MaxDuration)

        assert(!detailsFuture.value.get.get.translations(0).body.isEmpty)
    }

    test("ArticleViewer.searchByKeyword"){
        val KEYWORD = "help"
        val EXPECTED_IDS = Set[String]("1", "2")

        val searchFuture = av.searchByKeyword(KEYWORD)
        Await.result(searchFuture, MaxDuration)

        var resultIds = Set[String]()
        for( queryResult <- searchFuture.value.get.get){

            resultIds += queryResult.id

        }

        assert(EXPECTED_IDS.diff(resultIds).isEmpty)

    }

}
