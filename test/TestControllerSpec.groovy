

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


