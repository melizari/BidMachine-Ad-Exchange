import play.api.mvc.EssentialFilter
import play.filters.csrf.CSRFComponents
import play.filters.headers.SecurityHeadersComponents

trait ApiGatewayHttpFiltersComponents
  extends CSRFComponents
  with SecurityHeadersComponents {

  def httpFilters: List[EssentialFilter] = securityHeadersFilter :: Nil

}
