package ru.bmstu.rk9.rao.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ LinearProcessTest.class, QueueProcessTest.class,
		BranchedProcessTest.class })
public class ProcessTestSuite {

}
