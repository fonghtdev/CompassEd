$script:DEV_ENV = @{
  MYSQL_HOST = "localhost"
  MYSQL_PORT = "3306"
  MYSQL_USER = "root"
  MYSQL_PASSWORD = "1234"
  MYSQL_DATABASE = "compassed"

  JWT_SECRET = "this-is-a-very-strong-jwt-secret-min-32"
  GOOGLE_CLIENT_ID = "759011701592-v4npda3hrmcrsh8s8cnq2jh5jcuc755o.apps.googleusercontent.com"
  ADMIN_EMAIL = ""
  ADMIN_PASSWORD = ""

  MAIL_HOST = "smtp.gmail.com"
  MAIL_PORT = "587"
  MAIL_USERNAME = "compassed.edu@gmail.com"
  MAIL_PASSWORD = "mulvscrsplqufrig"
  MAIL_FROM = "CompassEd"

  BE_PORT = "8080"
  FE_PORT = "3000"
}
