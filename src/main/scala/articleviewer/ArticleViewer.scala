package articleviewer

import com.softwaremill.sttp._
import com.softwaremill.sttp.json4s.asJson
import com.softwaremill.sttp.asynchttpclient.future.AsyncHttpClientFutureBackend

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.blocking


class ArticleViewer{
  val apiKey = "ac63f04b011f792e6119591b7234cf7d"
  val JWT = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2FwcC5lbGV2aW8tc3RhZ2luZy5jb20iLCJzdWIiOiI1ZDE" +
    "zZmM2MTFiZDY1IiwiZXhwIjozMTM4NDEyMTEwLCJpYXQiOjE1NjE2MTIxMTAsImp0aSI6InVxZGNnNHJmbWozY3IwNzEyYWQwamp2YjlnYzFqbWF" +
    "1IiwKICAidXNlck5hbWUiIDogImphcmVkcm9oZUBnbWFpbC5jb20iLAogICJ1c2VySWQiIDogMTMwNDEsCiAgInNjb3BlIiA6IFsgInJlYWQ6YXJ" +
    "0aWNsZSIgXQp9.4RyWgzLylgVO4e-SIOevoo6AdKnwn3XASEmrqh9yVvw"


  val PAGE_SIZE = 3


  implicit val backend = AsyncHttpClientFutureBackend()
  implicit val serialization =  org.json4s.native.Serialization


  def makeReqeust[T](uri: Uri)(implicit m: Manifest[T])   = {

      val request = sttp
      .header("x-api-key", apiKey)
      .header("Authorization", "Bearer " + JWT)
      .get(uri)
      .response(asJson[T])

    val response = request.send()

    response

  }


  def getNumPages(): Future[Int] = {

    val response = makeReqeust[ArticlesResponse](
      uri"https://api.elevio-staging.com/v1/articles?page_size=$PAGE_SIZE")


    val p = Promise[Int]()

    response.onComplete({

      case Success(resp) => p.success(resp.body.getOrElse().asInstanceOf[ArticlesResponse].total_pages)
      case Failure(e) => p.failure(e)

    }
    )

    p.future

  }

  def getArticlesForPage(pageState: PageState): Future[List[Article]] = {

    val response = makeReqeust[ArticlesResponse](
      uri"https://api.elevio-staging.com/v1/articles?page=${pageState.pageNumber}&page_size=$PAGE_SIZE")

    val p = Promise[List[Article]]()

    response onComplete {

      case Success(resp) => p.success(resp.body.getOrElse().asInstanceOf[ArticlesResponse].articles)
      case Failure(e) => p.failure(e)

    }

  p.future

  }

  def getArticleDetails(articleID: Int): Future[ArticleDetail] ={

    val response = makeReqeust[ArticleDetailResponse](
      uri"https://api.elevio-staging.com/v1/articles/$articleID")

    val p = Promise[ArticleDetail]()

    response onComplete {

      case Success(resp) => p.success(resp.body.getOrElse().asInstanceOf[ArticleDetailResponse].article)
      case Failure(e) => p.failure(e)

    }

    p.future

//    if (response.body.isRight) {
//        response.body.getOrElse().asInstanceOf[ArticleDetailResponse].article
//    } else{
//       null
//    }

  }

  def searchByKeyword(keyword: String): Future[List[QueryResult]] ={

    val response = makeReqeust[QueryResponse](uri"https://api.elevio-staging.com/v1/search/en/?query=$keyword")

    val p = Promise[List[QueryResult]]()

    response onComplete {

      case Success(resp) => p.success(resp.body.getOrElse().asInstanceOf[QueryResponse].results)
      case Failure(e) => p.failure(e)

    }

    p.future


//    if (response.body.isRight) {
//
//      response.body.getOrElse().asInstanceOf[QueryResponse].results
//
//    } else{
//
//      "Error"
//      List[QueryResult]()
//
//    }


  }



}

