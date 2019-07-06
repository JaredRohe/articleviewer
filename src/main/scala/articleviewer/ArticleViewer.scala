package articleviewer

import java.util.Date

import com.softwaremill.sttp._
import com.softwaremill.sttp.asynchttpclient.future.AsyncHttpClientFutureBackend
import com.softwaremill.sttp.json4s.asJson

class ArticleViewer{
  val apiKey = "ac63f04b011f792e6119591b7234cf7d"
  val JWT = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2FwcC5lbGV2aW8tc3RhZ2luZy5jb20iLCJzdWIiOiI1ZDE" +
    "zZmM2MTFiZDY1IiwiZXhwIjozMTM4NDEyMTEwLCJpYXQiOjE1NjE2MTIxMTAsImp0aSI6InVxZGNnNHJmbWozY3IwNzEyYWQwamp2YjlnYzFqbWF" +
    "1IiwKICAidXNlck5hbWUiIDogImphcmVkcm9oZUBnbWFpbC5jb20iLAogICJ1c2VySWQiIDogMTMwNDEsCiAgInNjb3BlIiA6IFsgInJlYWQ6YXJ" +
    "0aWNsZSIgXQp9.4RyWgzLylgVO4e-SIOevoo6AdKnwn3XASEmrqh9yVvw"


  val PAGE_SIZE = 3


//implicit val sttpBackend = AsyncHttpClientFutureBackend()
  implicit val backend = HttpURLConnectionBackend()
  implicit val serialization =  org.json4s.native.Serialization


  val numPages = getNumPages()


  def makeReqeust[T](uri: Uri)(implicit m: Manifest[T])   = {

      val request = sttp
      .header("x-api-key", apiKey)
      .header("Authorization", "Bearer " + JWT)
      .get(uri)
      .response(asJson[T])

    val response = request.send()

    response

  }


  def getNumPages(): Int = {

    implicit val sttpBackend = AsyncHttpClientFutureBackend()

    val response = makeReqeust[ArticlesResponse](
      uri"https://api.elevio-staging.com/v1/articles?page_size=$PAGE_SIZE")

    //response.onComplete()

    if( response.body.isRight) {

      response.body.getOrElse().asInstanceOf[ArticlesResponse].total_pages
    }else{

      -1
    }

  }

  def getArticlesForPage(pageState: PageState): List[Article] = {

    val response = makeReqeust[ArticlesResponse](
      uri"https://api.elevio-staging.com/v1/articles?page=${pageState.pageNumber}&page_size=$PAGE_SIZE")

    if (response.body.isRight) {

      response.body.getOrElse().asInstanceOf[ArticlesResponse].articles

    }

    else{

      print("Error");
      List[Article]()
    }

  }

  def getArticleDetails(articleID: Int): ArticleDetail ={

    val response = makeReqeust[ArticleDetailResponse](
      uri"https://api.elevio-staging.com/v1/articles/$articleID")

    if (response.body.isRight) {
        response.body.getOrElse().asInstanceOf[ArticleDetailResponse].article
    } else{
       null
    }

  }

  def searchByKeyword(keyword: String): List[QueryResult] ={

    val response = makeReqeust[ArticlesResponse](uri"https://api.elevio-staging.com/v1/search/en/?query=$keyword")

    if (response.body.isRight) {

      response.body.getOrElse().asInstanceOf[QueryResponse].results

    } else{

      "Error"
      List[QueryResult]()

    }

  }



}

