import org.junit.*;

import play.test.WithBrowser;
import play.twirl.api.Content;
import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;


/**
*
* Simple (JUnit) tests that can call all parts of a play app.
* If you are interested in mocking a whole application, see the wiki for more details.
*
*/
public class ApplicationTest extends WithBrowser {

    @Test
    public void simpleCheck() {
        int a = 1 + 1;
        assertThat(a).isEqualTo(2);
    }

    @Test
    public void renderTemplate() {
    	String activeTab="HOME";
    	
        Content html = views.html.index.render(null, activeTab);
        assertThat(contentType(html)).isEqualTo("text/html");
        //Logger.error(html.toString());
        assertThat(contentAsString(html)).contains("Your new application is ready"); // correct page
        assertThat(contentAsString(html)).contains("Marine Noise Registry Service"); // correct language
    }
}
