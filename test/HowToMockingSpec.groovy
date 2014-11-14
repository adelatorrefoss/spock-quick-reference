
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


        // or

        Publisher publisher = new Publisher()
        def subscriber = Mock(Subscriber)
        publisher.subscriber = subscriber
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


            1 * subscriber.receive("hello")     // an argument that is equal to the String "hello"
            1 * subscriber.receive(!"hello")    // an argument that is unequal to the String "hello"
            1 * subscriber.receive()            // the empty argument list (would never match in our example)
            1 * subscriber.receive(_)           // any single argument (including null)
            1 * subscriber.receive(*_)          // any argument list (including the empty argument list)
            1 * subscriber.receive(!null)       // any non-null argument
            1 * subscriber.receive(_ as String) // any non-null argument that is-a String
            1 * subscriber.receive({ it.size() > 3 }) // an argument that satisfies the given predicate
                                                      // (here: message length is greater than 3)

            1 * subscriber._(*_)     // any method on subscriber, with any argument list
            1 * subscriber._         // shortcut for and preferred over the above

            1 * _._                  // any method call on any mock object
            1 * _                    // shortcut for and preferred over the above


            1 * subscriber.receive("hello") // demand one 'receive' call on `subscriber`
            _ * auditing._                  // allow any interaction with 'auditing'
            0 * _                           // don't allow any other interaction



    }


    // Stubs

    def test2() {
        setup:
            subscriber.receive(_) >> "ok"

            subscriber.receive(_) >>> ["ok", "error", "error", "ok"]

            subscriber.receive(_) >>> ["ok", "fail", "ok"] >> { throw new InternalError() } >> "ok"



    }


    // Mocking and Stubbing

    def test3() {
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



}

