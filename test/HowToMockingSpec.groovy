
// imports for unit testing
import spock.lang.*
import grails.plugin.spock.*
import grails.test.mixin.*
import grails.buildtestdata.mixin.Build
import spock.util.mop.ConfineMetaClassChanges

// example for metaclass in setup
import com.project.User

@Build([User, City])
@Mock([User, UserRelation])
@ConfineMetaClassChanges([User])
class MockingHowtoSpec extends Specification {

    Publisher publisher = new Publisher()
    Subscriber subscriber = Mock()
    // o
    def subscriber = Mock(Subscriber)

    // o declare and specific interaction at the same time
    Subscriber subscriber = Mock {
        1 * receive("hello")
        1 * receive("goodbye")
    }

    def setup() {
        publisher.subscribers << subscriber // << is a Groovy shorthand for List.add()
        publisher.subscribers << subscriber2

        service.otherService = subscriber
    }



    // setup for metaclass User
    def setup() {
        //mock encodePassword
        User.metaClass.encodePassword = { return 'aa'}
        User.metaClass.generateSlug = { return 'aa'}

        User.build() // works with annotation


    }


    def "should send messages to all subscribers"() {
        when:
            publisher.send("hello")

        then:
            1 * subscriber.receive("hello")
            1 * subscriber2.receive("hello")

            1 * subscriber.receive("hello")      // exactly one call
            0 * subscriber.receive("hello")      // zero calls
            (1..3) * subscriber.receive("hello") // between one and three calls (inclusive)
            (1.._) * subscriber.receive("hello") // at least one call
            (_..3) * subscriber.receive("hello") // at most three calls
            _ * subscriber.receive("hello")      // any number of calls, including zero
                                                 // (rarely needed; see 'Strict Mocking')

            1 * _.receive("hello")          // a call to any mock object

            1 * subscriber.status // same as: 1 * subscriber.getStatus()

            1 * subscriber.setStatus("ok") // NOT: 1 * subscriber.status = "ok"


            1 * subscriber.receive()            // the empty argument list (would never match in our example)
            1 * subscriber.receive(_)           // any single argument (including null)
            1 * subscriber.receive(*_)          // any argument list (including the empty argument list)

            // type
            1 * subscriber.receive(_ as String) // any non-null argument that is-a String

            // conditionals
            1 * subscriber.receive("hello")     // an argument that is equal to the String "hello"
            1 * subscriber.receive(!"hello")    // an argument that is unequal to the String "hello"
            1 * subscriber.receive(!null)       // any non-null argument
            1 * subscriber.receive({ it.size() > 3 }) // an argument that satisfies the given predicate
                                                      // (here: message length is greater than 3)
                                                      
            // detailed check of parameter passed to function
            1 * userService.save({User user -> user.name == 'Lucas'})
    

            1 * subscriber._(*_)     // any method on subscriber, with any argument list
            1 * subscriber._         // shortcut for and preferred over the above

            1 * _._                  // any method call on any mock object
            1 * _                    // shortcut for and preferred over the above


            1 * subscriber.receive("hello") // demand one 'receive' call on `subscriber`
            _ * auditing._                  // allow any interaction with 'auditing'
            0 * _                           // don't allow any other interaction



    }



    def "Order of execution"() {
        given:
            UserService service = Mock()
            Transaction transaction = Mock()
        when:
            service.save(new User())
            transaction.commit()
            
        then:
            1 * service.save(_ as User)
        then:
            1 * transaction.commit()
    }






    // Stubs

    def test2() {
        setup:
            subscriber.receive(_) >> "ok"

            subscriber.receive(_) >>> ["ok", "error", "error", "ok"]

            subscriber.receive(_) >>> ["ok", "fail", "ok"] >> { throw new InternalError() } >> "ok"



    }

    def tests3() {
        when:
            publisher.send("hello")

        then:
            // Mocking and stubbing of the same method call has to happen in the same interaction.
            1 * subscriber.receive("message1") >> "ok"
            1 * subscriber.receive("message2") >> "fail"
    }


    def onlyStub() {
        setup:
            def subscriber = Stub(Subscriber)
            subscriber.receive(_) >> "ok"

            // declaring interactions at mock creation time
            def subscriber2 = Stub(Subscriber) {
                receive("message1") >> "ok"
                receive("message2") >> "fail"
            }

        then:
            // If a stub invocation matches a mandatory interaction (like 1 * foo.bar()), an InvalidSpecException is thrown.


    }


    // Advanced match of parameter
    
    def "should throw exception if user's name is Michael otherwise no exception should be thrown"() {
        given:
            UserService service = Stub()
            
            // Advanced match of parameter
            service.save({ User user -> 'Michael' == user.name }) >> {
                throw new IllegalArgumentException("We don't want you here, Micheal!")
            }
     
        when:
            User user = new User(name: 'Michael')
            service.save(user)
        then:
            thrown(IllegalArgumentException)
        
        when:
            User user2 = new User(name: 'Lucas')
            service.save(user2)
        then:
            notThrown(IllegalArgumentException)
    }
    
    
    
    // Spies
    
    def "creating spy from class"() {
        given:
            Transaction transaction = Stub(Transaction)
            UserService service = Spy(UserServiceImpl, constructorArgs: [transaction])
        expect:
            service.save(new User(name: 'Katherine'))
    }
    
    
    
    // Exceptions
    def 'exceptions'() {
        def "should throw IllegalArgumentException with proper message"() {
        when:
            throw new IllegalArgumentException("Does description matter?")
        then:
            def e = thrown(IllegalArgumentException)
            e.message == "Does description matter?"
    }


    // Assignments at the level of classJava
    // Reset every test
    // Shared
    
    // *************************************
    // Please note that Spock does not guarantee that tests specified in a class will be 
    // executed in the order they appear in the file.
    // *************************************
    
    private List<Integer> objectUnderTest = []
    
    @Shared
    private List<Integer> objectUnderTestShared = []
     
    def "test 1"() {
        when:
            objectUnderTest.add(1)
            objectUnderTestShared.add(1)
        then:
            objectUnderTest.size() == 1
            objectUnderTestShared.size() == 1
    }
     
    def "test 2"() {
        when:
            objectUnderTest.add(1)
            objectUnderTestShared.add(1)
        then:
            objectUnderTest.size() == 1
            objectUnderTestShared.size() == 2
    }
    
    



    // Mocks from Grails
    // 
    // The Mock annotation creates mock version of any collaborators. 
    // There is an in-memory implementation of GORM that will simulate most interactions 
    // with the GORM API. For those interactions that are not automatically mocked you can 
    // use the built in support for defining mocks and stubs programmatically. 
    //
    // For example:

    void testSearch() {
          def control = mockFor(SearchService)
          control.demand.searchWeb { String q -> ['mock results'] }
          control.demand.static.logResults { List results ->  }
          controller.searchService = control.createMock()
          controller.search()
          assert controller.response.text.contains "Found 1 results"
    }
    
    
    // from : http://grails.org/doc/2.4.3/guide/testing.html#unitTesting
    
    
    import grails.test.mixin.TestFor
import spock.lang.Specification
@TestFor(BookController)
@Mock(Book)
class BookControllerSpec extends Specification {

    void "test search"() {
        given:
        def searchMock = mockFor(SearchService)
        searchMock.demand.searchWeb { String q -> ['first result', 'second result'] }
        searchMock.demand.static.logResults { List results ->  }
        controller.searchService = searchMock.createMock()

        when:
        controller.search()

        then:
        controller.response.text.contains "Found 2 results"
    }
}

}

