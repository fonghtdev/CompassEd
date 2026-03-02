$script:DEV_ENV = @{
  MYSQL_HOST = "localhost"
  MYSQL_PORT = "3306"
  MYSQL_USER = "root"
  MYSQL_PASSWORD = "1234"
  MYSQL_DATABASE = "compassed"

  JWT_SECRET = "***REMOVED***"
  OPENAI_API_KEY = ""
  GOOGLE_CLIENT_ID = "759011701592-v4npda3hrmcrsh8s8cnq2jh5jcuc755o.apps.googleusercontent.com"
  ADMIN_EMAIL = ""
  ADMIN_PASSWORD = ""

  MAIL_HOST = "smtp.gmail.com"
  MAIL_PORT = "587"
  MAIL_USERNAME = "compassed.edu@gmail.com"
  MAIL_PASSWORD = "***REMOVED***"
  MAIL_FROM = "CompassEd"

  PAYOS_ENABLED = "true"
  CHECKOUT_QR_ACCOUNT_NAME = "COMPASSED"
  PAYOS_CLIENT_ID = "***REMOVED***"
  PAYOS_API_KEY = "***REMOVED***"
  PAYOS_CHECKSUM_KEY = "***REMOVED***"
  PAYOS_BASE_URL = "https://api-merchant.payos.vn"
  PAYOS_RETURN_URL = "https://compassed.io.vn/checkout"
  PAYOS_CANCEL_URL = "https://compassed.io.vn/checkout"
  PAYOS_CHECK_COOLDOWN_SECONDS = "15"

  BE_PORT = "8080"
  FE_PORT = "3000"
}
