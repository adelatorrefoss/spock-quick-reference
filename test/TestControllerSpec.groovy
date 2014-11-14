// Extract from http://grails.org/doc/latest/guide/testing.htmls

import grails.test.mixin.TestFor
import spock.lang.Specification

// have 'controller' variable
@TestFor(BookController)

// have GORM mocking for this objects
@Mock([Book, Author])

class BookControllerSpec extends Specification {

    void "test search"() {
        given:
        def searchMock = mockFor(SearchService)
        searchMock.demand.searchWeb { String q -> ['first result', 'second result'] }
        searchMock.demand.static.logResults { List results ->  }

        controller.searchService = searchMock.createMock()


        // request


        when:
        controller.search()

        then:

        // response


        controller.response.text.contains "Found 2 results"
    }
}





// doWithSpring and doWithConfig callback methods, FreshRuntime annotation


import grails.test.mixin.support.GrailsUnitTestMixin
import org.junit.ClassRule
import org.junit.rules.TestRule

import spock.lang.Shared;
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class StaticCallbacksSpec extends Specification {
    static doWithSpring = {
        myService(MyService)
    }

    // config
    static doWithConfig(c) {
        c.myConfigValue = 'Hello'
    }

    def "grailsApplication is not null"() {
        expect:
        grailsApplication != null
    }

    def "doWithSpring callback is executed"() {
        expect:
        grailsApplication.mainContext.getBean('myService') != null
    }


    // config
    def "doWithConfig callback is executed"(){
        expect:
        config.myConfigValue == 'Hello'
    }
}


// more about beans
grails.test.runtime.FreshRuntime
org.codehaus.groovy.grails.commons.InstanceFactoryBean
grails.test.runtime.DirtiesRuntime

// Loading application beans in unit tests
// Adding static loadExternalBeans = true

import spock.lang.Issue
import spock.lang.Specification
import grails.test.mixin.support.GrailsUnitTestMixin
@TestMixin(GrailsUnitTestMixin)
class LoadExternalBeansSpec extends Specification {
    static loadExternalBeans = true

    void "should load external beans"(){
        expect:
        applicationContext.getBean('simpleBean') == 'Hello world!'
    }
}




// ***********************************************
//    GENERAL
// ***********************************************

// Test class
class SimpleController {
    def hello() {
        render "hello"
    }

    def index() {
        redirect action: 'hello'
    }
}


import grails.test.mixin.TestFor
import spock.lang.Specification
import static javax.servlet.http.HttpServletResponse.*

@TestFor(SimpleController)
class SimpleControllerSpec extends Specification {


    // response

    void "test hello"() {
        when:
        controller.hello()

        then:
        response.text == 'hello'


        response.redirectedUrl == '/simple/hello'

        response.json.book == 'Great'

        response.status == SC_METHOD_NOT_ALLOWED


    }


    //params

    void 'test list'() {
        when:
        params.sort = 'name'
        params.max = 20
        params.offset = 0
        controller.list()

        then:
        // …

    // methods
        when:
        request.method = 'POST'


    // ajax
        when:
        request.method = 'POST'
        request.makeAjaxRequest()
        controller.getPage()

    }
}



// model

class SimpleController {
    def home() {
        render view: "homePage", model: [title: "Hello World"]
    }
    …
}

import grails.test.mixin.TestFor
import spock.lang.Specification
@TestFor(SimpleController)
class SimpleControllerSpec extends Specification {

    void 'test home'() {
        when:
        controller.home()

        then:

        // check view and model
        view == '/simple/homePage'
        model.title == 'Hello World'
    }
}

// ***************
// test templates

class SimpleController {
    def display() {
        render template:"snippet"
    }
}

//In this example the controller will look for a template in grails-app/views/simple/_snippet.gsp. You can test this as follows:

import grails.test.mixin.TestFor
import spock.lang.Specification
@TestFor(SimpleController)
class SimpleControllerSpec extends Specification {

    void 'test display'() {
        when:
        controller.display()

        then:
        response.text == 'contents of the template'
    }
}

// However, you may not want to render the real template, but just test that is was rendered. In this case you can provide mock Groovy Pages:

import grails.test.mixin.TestFor
import spock.lang.Specification
@TestFor(SimpleController)
class SimpleControllerSpec extends Specification {

    void 'test display with mock template'() {
        when:
        views['/simple/_snippet.gsp'] = 'mock template contents'
        controller.display()

        then:
        response.text == 'mock template contents'
    }
}




// **************
// json

// controller action
def renderJson() {
    render(contentType:"application/json") {
        book = "Great"
    }
}

import grails.test.mixin.TestFor
import spock.lang.Specification
@TestFor(SimpleController)
class SimpleControllerSpec extends Specification {

    void 'test render json'() {
        when:
        controller.renderJson()

        then:
        response.text == '{"book":"Great"}'
        response.json.book == 'Great'
    }
}




// json request

def consumeBook(Book b) {
    render "The title is ${b.title}."
}

import grails.test.mixin.TestFor
import spock.lang.Specification
@TestFor(SimpleController)
@Mock([Book])
class SimpleControllerSpec extends Specification {

    void 'test consume book json'() {
        when:
        request.json = new Book(title: 'Shift')
        controller.consumeBook()

        then:
        response.text == 'The title is Shift.'
    }
}



// json request not using Grails bindings

def consume() {
    request.withFormat {
        xml {
            render "The XML Title Is ${request.XML.@title}."
        }
        json {
            render "The JSON Title Is ${request.JSON.title}."
        }
    }
}


import grails.test.mixin.TestFor
import spock.lang.Specification
@TestFor(SimpleController)
class SimpleControllerSpec extends Specification {

    void 'test consume json'() {
        when:
        request.json = '{title:"The Stand"}'
        controller.consume()

        then:
        response.text == 'The JSON Title Is The Stand.'
    }
}




// Testing Mime Type Handling


//You can test mime type handling and the withFormat method quite simply by setting the request's contentType attribute:

// controller action
def sayHello() {
    def data = [Hello:"World"]
    request.withFormat {
        xml { render data as grails.converters.XML }
        json { render data as grails.converters.JSON }
        html data
    }
}

import grails.test.mixin.TestFor
import spock.lang.Specification
@TestFor(SimpleController)
class SimpleControllerSpec extends Specification {

    void 'test say hello xml'() {
        when:
        request.contentType = 'application/xml'
        controller.sayHello()

        then:
        response.text == '<?xml version="1.0" encoding="UTF-8"?><map><entry key="Hello">World</entry></map>'
    }

    void 'test say hello json'() {
        when:
        request.contentType = 'application/json'
        controller.sayHello()

        then:
        response.text == '{"Hello":"World"}'
    }
}

//There are constants provided by ControllerUnitTestMixin for all of the common common content types as shown below:

import grails.test.mixin.TestFor
import spock.lang.Specification
@TestFor(SimpleController)
class SimpleControllerSpec extends Specification {

    void 'test say hello xml'() {
        when:
        request.contentType = XML_CONTENT_TYPE
        controller.sayHello()

        then:
        response.text == '<?xml version="1.0" encoding="UTF-8"?><map><entry key="Hello">World</entry></map>'
    }

    void 'test say hello json'() {
        when:
        request.contentType = JSON_CONTENT_TYPE
        controller.sayHello()

        then:
        response.text == '{"Hello":"World"}'
    }
}



// The defined constants are listed below:

// Constant         Value
ALL_CONTENT_TYPE	"*/*"
FORM_CONTENT_TYPE	application/x-www-form-urlencoded
MULTIPART_FORM_CONTENT_TYPE	multipart/form-data
HTML_CONTENT_TYPE	text/html
XHTML_CONTENT_TYPE	application/xhtml+xml
XML_CONTENT_TYPE	application/xml
JSON_CONTENT_TYPE	application/json
TEXT_XML_CONTENT_TYPE	text/xml
TEXT_JSON_CONTENT_TYPE	text/json
HAL_JSON_CONTENT_TYPE	application/hal+json
HAL_XML_CONTENT_TYPE	application/hal+xml
ATOM_XML_CONTENT_TYPE	application/atom+xml





// Testing File Upload



//You use the GrailsMockMultipartFile class to test file uploads. For example consider the following controller action:

def uploadFile() {
    MultipartFile file = request.getFile("myFile")
    file.transferTo(new File("/local/disk/myFile"))
}

//To test this action you can register a GrailsMockMultipartFile with the request:

import grails.test.mixin.TestFor
import spock.lang.Specification
import org.codehaus.groovy.grails.plugins.testing.GrailsMockMultipartFile

@TestFor(SimpleController)
class SimpleControllerSpec extends Specification {

    void 'test file upload'() {
        when:
        def file = new GrailsMockMultipartFile('myFile', 'some file contents'.bytes)
        request.addFile file
        controller.uploadFile()

        then:
        file.targetFileLocation.path == '/local/disk/myFile'
    }
}

// The GrailsMockMultipartFile constructor arguments are the name and contents of the file. It has a mock implementation of the transferTo method that simply records the targetFileLocation and doesn't write to disk.



// Testing Command Objects
// Special support exists for testing command object handling with the mockCommandObject method. For example consider the following action:

class SimpleController {
    def handleCommand(SimpleCommand simple) {
        if(simple.hasErrors()) {
            render 'Bad'
        } else {
            render 'Good'
        }
    }
}
class SimpleCommand {
    String name

    static constraints = {
        name blank: false
    }
}

// To test this you mock the command object, populate it and then validate it as follows:

import grails.test.mixin.TestFor
import spock.lang.Specification
@TestFor(SimpleController)
class SimpleControllerSpec extends Specification {

    void 'test valid command object'() {
        given:
        def simpleCommand = new SimpleCommand(name: 'Hugh')
        simpleCommand.validate()

        when:
        controller.handleCommand(simpleCommand)

        then:
        response.text == 'Good'
    }

    void 'test invalid command object'() {
        given:
        def simpleCommand = new SimpleCommand(name: '')
        simpleCommand.validate()

        when:
        controller.handleCommand(simpleCommand)

        then:
        response.text == 'Bad'
    }
}





