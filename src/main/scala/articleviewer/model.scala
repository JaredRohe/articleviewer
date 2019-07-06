package articleviewer

import java.util.Date

case class PageState(
                      pageNumber: Int
                    )

case class ArticleDetailResponse(
                                  article: ArticleDetail
                                )

case class Article(
                    updated_at: Date,
                    title: String,
                    notes: String,
                    id: Int,
                    keywords: List[String]
                  )

case class Translation(
                        id: Int,
                        language_id: String,
                        title: String,
                        body: String
                      )

case class ArticleDetail(
                          id: Int,
                          title: String,
                          author: User,
                          source: String,
                          external_id: String,
                          order: Int,
                          category_id: Int,
                          access: String,
                          access_emails: List[String],
                          access_domains: List[String],
                          access_groups: List[String],
                          smart_groups: List[User],
                          keywords: List[String],
                          notes: String,
                          status: String,
                          last_publisher: User,
                          last_published_at: Date,
                          contributors: List[User],
                          editor_version: String,
                          created_at: Date,
                          updated_at: Date,
                          translations: List[Translation]
                        )

case class User(
                 id: Int,
                 name: String,
                 gravatar: String,
                 email: String

               )


case class ArticlesResponse(
                             total_pages: Int,
                             page_number: Int,
                             articles: List[Article]
                           )

case class QueryResponse(

                          queryTerm: String,
                          totalResults: Int,
                          totalPages: Int,
                          currentPage: Int,
                          count: Int,
                          results: List[QueryResult]
                        )

case class QueryResult(
                        id: String,
                        title: String
                      )

