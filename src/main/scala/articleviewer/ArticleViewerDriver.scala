package articleviewer

import org.json4s.DefaultFormats
import org.json4s.native.Serialization.writePretty

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await}
import scala.concurrent.duration._
import scala.language.postfixOps

object ArticleViewerDriver extends App {

  val articleViewer =  new ArticleViewer()
  val MaxDuration = 60 seconds
  var numPages = {
    val numPagesF = articleViewer.getNumPages()
    Await.result(numPagesF, MaxDuration)
    numPagesF.value.get.get
  }

  //var articles: List[Article] = _
  var pageState = PageState(1, List[Article](), true)
  //var loading = true

  driver()

  def driver(): Unit = {

    selectPage(page = 1)

    var command = ""
    var input: Int = 0
    var searchQuery: String = ""


    do {
      printPageSelector(pageState)
      printAriclesOnPage(pageState)
      command = parseCommand()

      command match{

        case "q" => {
          print("Bye.");
          articleViewer.backend.close()
        }
        case "d" => {
          print("Enter Article Number: ");
          input = scala.io.StdIn.readInt();
          displayArticleDetails(input)
        }
        case "s" => {
          print("Enter Keyword To Query: ");
          searchQuery = scala.io.StdIn.readLine();
          displaySearchResults(searchQuery)
        }
        case "p" => {
          print("Enter Page Number: ");
          input = scala.io.StdIn.readInt();
          selectPage(input)
        }

      }

      if(command != "q") println("\n"*3)

    } while(command !="q")

  }

  def displaySearchResults(keyword: String): Unit = {

    val searchResultsF = articleViewer.searchByKeyword(keyword)
    println("Fetching Search Results...")
    Await.result(searchResultsF, MaxDuration)

    println("--Search Results--")
    for (result <- searchResultsF.value.get.get) {
      print("ID: "); println(result.id)
      print("Title: "); println(result.title)
    }

  }

  def displayArticleDetails(articleNumber: Int): Unit ={
    implicit val jsonFormats = DefaultFormats
    val articleIndex = articleNumber -1

    if(articleIndex >=0 && articleIndex < pageState.articles.size ) {
      val articleID = pageState.articles(articleIndex).id
      val articleDetailsFuture = articleViewer.getArticleDetails(articleID)

      println("Fetching Article Details...")
      Await.result(articleDetailsFuture, MaxDuration)
      println(writePretty(articleDetailsFuture.value.get.get))

    }
    else{
      println("Invalid Article Selection.")
    }
  }

  def selectPage(page: Int) = {

    if( page > numPages || page < 0) {
        println("Invalid page selection")

    }else{
      val loadingPogram= pageState.loading
      pageState = PageState(page, List[Article](), false)
      val articlesF =  articleViewer.getArticlesForPage(pageState)

      articlesF onComplete {
        case Success(articles) => pageState = PageState(page, articles, false)
        case Failure(e) => e.getMessage()
      }

      if(loadingPogram){println("Loading...")}
      else{ println("Switching Page...")}
      Await.result(articlesF, MaxDuration)
    }
  }


  def printAriclesOnPage(pageState: PageState): Unit = {

    if(pageState.articles != null) {
      pageState.articles.zipWithIndex.foreach {
        case (article, articleNumber) => println(articleNumber + 1 + ".) " + article.title)
      }
      println()
    }
    else{
      println("NO ARTICLES TO DISPLAY")
    }
  }


  def printPageSelector(pageState: PageState) = {

    println("-----------------------")
    print("PAGES: ")
    for(pageNumber <- 1 until this.numPages + 1){
      print("[" + (pageNumber ).toString() + "]")
      if (pageNumber == pageState.pageNumber){
        print("* ")
      }
      else print(" ")
    }

    println()
    println("-----------------------")
    println("-- Aricles for Page " + (pageState.pageNumber).toString + " --")

  }


  def parseCommand(): String = {
    println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%")
    println("Enter one of the following commands:")
    println("d -- view details of an article")
    println("p -- switch page" )
    println("s -- Search for an article")
    println("q -- exit the program")
    print("\n"*2)

    var input = scala.io.StdIn.readLine("command>> ").strip()


    val options = List("d", "p", "s", "q")

    var validCommand=false
    while(!validCommand){

      if(options.contains(input)){

        validCommand=true

      }else{
        println(input + " is not one of [d,p,s,q]")
        println("Please enter a valid command.")

        input = scala.io.StdIn.readLine("command>> ").strip()

      }
    }

    input

  }

}
