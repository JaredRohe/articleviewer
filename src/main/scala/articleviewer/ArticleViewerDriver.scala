package articleviewer

import org.json4s.DefaultFormats
import org.json4s.native.Serialization.writePretty

object ArticleViewerDriver extends App {

  val articleViewer = new ArticleViewer()
  var articles: List[Article] = _
  var viewingPageNumber = 1
  selectPage(viewingPageNumber)
  var inUse=true
  var command = ""
  var input: Int = 0
  var searchQuery: String = _
  implicit val jsonFormats = DefaultFormats

  var pageState = PageState(viewingPageNumber)

  while(inUse) {
    printPageSelector(pageState)
    printAriclesOnPage(pageState)
    command = parseCommand()

    command match{

      case "q" => {print("Bye."); inUse = false; articleViewer.backend.close()}
      case "d" => {print("Enter article Number: "); input = scala.io.StdIn.readInt(); displayArticleDetails(input) }
      case "s" => {print("Enter keyword to query: "); searchQuery = scala.io.StdIn.readLine(); displaySearchResults(searchQuery)}
      case "p" => { print("Enter page number: "); input = scala.io.StdIn.readInt(); selectPage(input)}

    }

    println("\n"*3)

  }

  def displaySearchResults(keyword: String): Unit = {

      val searchResults = articleViewer.searchByKeyword(keyword)

      println("--Search Results--")
      for(result <- searchResults){

        print("ID: ");println(result.id)
        print("Title: "); println(result.title)
      }

  }

  def displayArticleDetails(articleNumber: Int): Unit ={
    val articleIndex = articleNumber -1
    val articleID = articles(articleIndex).id
    val articleDetails = articleViewer.getArticleDetails(articleID)
    println(writePretty(articleDetails))

  }

  def selectPage(page: Int) = {

    if( page > articleViewer.numPages || page < 0) {

        println("Invalid page selection")

    }else{

      pageState = PageState(page)
      articles =  articleViewer.getArticlesForPage(pageState)

    }

  }


  def printAriclesOnPage(pageState: PageState): Unit = {

    articles.zipWithIndex.foreach{

      case(article, articleNumber) => println(articleNumber +1 + ".) " + article.title)

    }

    println()

  }


  def printPageSelector(pageState: PageState) = {

    println("-----------------------")
    print("PAGES: ")
    for(pageNumber <- 1 until articleViewer.numPages + 1){
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
