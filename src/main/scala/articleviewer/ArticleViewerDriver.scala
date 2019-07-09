package articleviewer

import org.json4s.DefaultFormats
import org.json4s.native.Serialization.writePretty

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

object ArticleViewerDriver extends App {

  val articleViewer = new ArticleViewer()
  val MaxDuration = 60 seconds
  var numPages = {
    val numPagesF = articleViewer.getNumPages
    Await.result(numPagesF, MaxDuration)
    numPagesF.value.get.get
  }

  var pageState = PageState(1, List[Article](), loading = true)
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

      command match {
        case "q" =>
          print("Bye.")
          articleViewer.backend.close()
        case "d" =>
          print("Enter Article Number: ")
          input = scala.io.StdIn.readInt()
          displayArticleDetails(input)
        case "s" =>
          print("Enter Keyword To Query: ")
          searchQuery = scala.io.StdIn.readLine()
          displaySearchResults(searchQuery)
        case "p" =>
          print("Enter Page Number: ")
          input = scala.io.StdIn.readInt()
          selectPage(input)
      }

      if (command != "q") println("\n" * 3)

    } while (command != "q")

  }

  def displaySearchResults(keyword: String): Unit = {
    if (!keyword.isEmpty) {
      val searchResultsF = articleViewer.searchByKeyword(keyword)
      println("Fetching Search Results...")
      Await.result(searchResultsF, MaxDuration)

      println("--Search Results--")
      if (searchResultsF.value.get.isSuccess) {
        val searchResults = searchResultsF.value.get.get
        if (searchResults.isEmpty) println("No Results Found")

        for (result <- searchResults) {
          print("ID: "); println(result.id)
          print("Title: "); println(result.title)
        }
      } else {
        println("Something went wrong when searching for results...")
        println(searchResultsF.value.get.failed.get.getMessage)

      }
    } else {
      println("Must Enter a Keyword")
    }

  }

  def displayArticleDetails(articleNumber: Int): Unit = {

    implicit val jsonFormats: DefaultFormats.type = DefaultFormats
    val articleIndex = articleNumber - 1

    if (articleIndex >= 0 && articleIndex < pageState.articles.size) {
      val articleID = pageState.articles(articleIndex).id
      val articleDetailsFuture = articleViewer.getArticleDetails(articleID)

      println("Fetching Article Details...")
      Await.result(articleDetailsFuture, MaxDuration)

      if (articleDetailsFuture.value.get.isSuccess)
        println(writePretty(articleDetailsFuture.value.get.get))
      else {
        println("Something went wrong when attempting to get article details...")
        println(articleDetailsFuture.value.get.failed.get.getMessage)
      }

    }
    else {
      println("Invalid Article Selection.")
    }
  }

  def selectPage(page: Int): Unit = {

    if (page > numPages || page < 0) {
      println("Invalid Page Selection")

    } else {
      val loadingPogram = pageState.loading
      pageState = PageState(page, List[Article](), loading = false)
      val articlesF = articleViewer.getArticlesForPage(pageState)

      articlesF onComplete {
        case Success(articles) => pageState = PageState(page, articles, loading = false)
        case Failure(e) => println("Something Went wrong when switching pages..."); println(e.getMessage)
      }

      if (loadingPogram) {
        println("Loading...")
      }
      else {
        println("Switching Page...")
      }
      Await.result(articlesF, MaxDuration)
    }
  }


  def printAriclesOnPage(pageState: PageState): Unit = {

    if (!pageState.articles.isEmpty) {
      pageState.articles.zipWithIndex.foreach {
        case (article, articleNumber) => println(articleNumber + 1 + ".) " + article.title)
      }
      println()
    }
    else {
      println("NO ARTICLES TO DISPLAY")
    }
  }


  def printPageSelector(pageState: PageState): Unit = {

    println("-----------------------")
    print("PAGES: ")
    for (pageNumber <- 1 until numPages + 1) {
      print("[" + pageNumber.toString + "]")
      if (pageNumber == pageState.pageNumber) {
        print("* ")
      }
      else print(" ")
    }

    println()
    println("-----------------------")
    println("-- Aricles for Page " + pageState.pageNumber.toString + " --")

  }


  def parseCommand(): String = {
    println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%")
    println("Enter One of the Following Commands:")
    println("d -- view details of an article")
    println("p -- switch page")
    println("s -- search for an article")
    println("q -- exit the program")
    print("\n" * 2)

    var input = scala.io.StdIn.readLine("command>> ").strip()

    val options = List("d", "p", "s", "q")
    var validCommand = false
    while (!validCommand) {

      if (options.contains(input)) {

        validCommand = true

      } else {
        if (!input.isEmpty) {
          println(input + " is not one of [d,p,s,q]")
          println("Please Enter a Valid Command.")
        }
        input = scala.io.StdIn.readLine("command>> ").strip()

      }
    }

    input

  }

}
