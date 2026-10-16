# Copy to dev-env.ps1 (git-ignored) and fill in the secrets.
$script:DEV_ENV = @{
  MYSQL_HOST = "localhost"
  MYSQL_PORT = "3306"
  MYSQL_USER = "root"
  MYSQL_PASSWORD = ""
  MYSQL_DATABASE = "compassed"

  JWT_SECRET = ""
  OPENAI_API_KEY = ""
  GOOGLE_CLIENT_ID = "418581116582-qet8ant9k0t319hkd3640dtsa54eg07v.apps.googleusercontent.com"
  ADMIN_EMAIL = ""
  ADMIN_PASSWORD = ""

  MAIL_HOST = "smtp.gmail.com"
  MAIL_PORT = "587"
  MAIL_USERNAME = ""
  MAIL_PASSWORD = ""
  MAIL_FROM = ""

  PAYOS_ENABLED = "false"
  CHECKOUT_QR_ACCOUNT_NAME = "COMPASSED"
  PAYOS_CLIENT_ID = ""
  PAYOS_API_KEY = ""
  PAYOS_CHECKSUM_KEY = ""
  PAYOS_BASE_URL = "https://api-merchant.payos.vn"
  PAYOS_RETURN_URL = "https://compassed.io.vn/checkout"
  PAYOS_CANCEL_URL = "https://compassed.io.vn/checkout"
  PAYOS_CHECK_COOLDOWN_SECONDS = "15"

  BE_PORT = "8080"
  FE_PORT = "3000"
  FE_API_BASE = "http://localhost:8080"
}
