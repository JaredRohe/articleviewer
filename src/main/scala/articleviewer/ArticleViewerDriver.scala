package articleviewer

import org.json4s.DefaultFormats
import org.json4s.native.Serialization.writePretty

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, blocking}
import scala.concurrent.duration._
import scala.language.postfixOps

object ArticleViewerDriver extends App {
  implicit val jsonFormats = DefaultFormats

  val articleViewer =  new ArticleViewer()
  var numPages = {
    val numPagesF = articleViewer.getNumPages()
    Await.result(numPagesF, 30 seconds)
    numPagesF.value.get.get
  }

  var inUse=true
  var loading=true
  var command = ""
  var input: Int = 0
  var searchQuery: String = _



  var articles: List[Article] = _
  selectPage(page = 1)




  var pageState = PageState(1)

  while(inUse) {
    printPageSelector(pageState)
    printAriclesOnPage(pageState)
    command = parseCommand()

    command match{

      case "q" => {
        print("Bye.");
        inUse = false;
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

    println("\n"*3)

  }

  def displaySearchResults(keyword: String): Unit = {

      val searchResultsF = articleViewer.searchByKeyword(keyword)
    println("Fetching search Results")

    Await.result(searchResultsF, 60 seconds)


    println("--Search Results--")
    for (result <- searchResultsF.value.get.get) {

      print("ID: "); println(result.id)
      print("Title: "); println(result.title)
    }

  }

  def displayArticleDetails(articleNumber: Int): Unit ={
    val articleIndex = articleNumber -1
    if(articleIndex < articles.size) {
      val articleID = articles(articleIndex).id
      val articleDetailsFuture = articleViewer.getArticleDetails(articleID)

      println("Fetching Article Details...")
//      articleDetailsFuture onComplete {
//
//        case Success(article) => {println(writePretty(article));}
//
//        }

      Await.result(articleDetailsFuture, 60 seconds)
      println(writePretty(articleDetailsFuture.value.get.get))

    }
    else{
      println("Invalid Article Selection.")

    }

  }

  def selectPage(page: Int) = {

    if( page > this.numPages || page < 0) {

        println("Invalid page selection")

    }else{

      pageState = PageState(page)
      val articlesF =  articleViewer.getArticlesForPage(pageState)

      articlesF onComplete {

        case Success(articles) => this.articles = articles
        case Failure(e) => e.getMessage()

      }
      if(loading){println("Loading..."); loading=false}
      else{ println("Switching Page...")}
      Await.result(articlesF, 60 seconds)

    }

  }


  def printAriclesOnPage(pageState: PageState): Unit = {

    if(articles != null) {
      articles.zipWithIndex.foreach {

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

  var performSearch = false

}
