# Security

Read carefully:

   * A self-signed key is provided called temporary.key/crt - DO NOT USE THIS IN PRODUCTION OR ANYWHERE. Browsers will complain if you use this.
   * THIS IS NOT SECURE!!!
   * PUTTING PRIVATE KEYS ON GITHUB FOR ANYTHING ELSE THAN A DEMONSTRATION IS CRAZY. DO NOT DO THIS.
   * TO SAVE YOURSELF, SET THE use http FIELDS TO true in the configuration file.
   * That isn't secure either...  
   * GENERATE YOUR OWN KEYS AND KEEP THEM SECURE and use https
   * TA10 runs over HTTP !!!!   
   * DO NOT USE TA10 IN UNSAFE MODE !!! (Even if you're not root!)
   * A10 signs claims, results, sessions etc...the keys are randomly generated each him the system is started and aren't recorded anywhere. There is some code to talk PKCS#11 and has been tested with a YubiHSM but it isn't used. Don't rely upon it, I haven't tested it and it is just placeholder at this time. Yes, I'll get arund to writing the proper functionality real soon now...volunteers?

Now read the section on the use of this software in a production environment.

# Use in a Production Environment

Don't.  This is not secure and many points where errors and exceptions should be captured are not implemented.


