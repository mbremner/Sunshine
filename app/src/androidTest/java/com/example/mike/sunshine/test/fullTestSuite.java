package com.example.mike.sunshine.test;


import android.test.suitebuilder.TestSuiteBuilder;

import junit.framework.Test;

/**
 * Created by mbremner on 8/9/2014.
 */
public class FullTestSuite {
    public static Test suite() {
        return new TestSuiteBuilder(FullTestSuite.class).includeAllPackagesUnderHere().build();
    }
        public FullTestSuite() {
            super();
        }
    }


