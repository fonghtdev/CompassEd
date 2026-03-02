$script:DEV_ENV = @{
  MYSQL_HOST = "localhost"
  MYSQL_PORT = "3306"
  MYSQL_USER = "root"
  MYSQL_PASSWORD = "1234"
  MYSQL_DATABASE = "compassed"

  JWT_SECRET = "this-is-a-very-strong-jwt-secret-min-32"
  OPENAI_API_KEY = ""
  GOOGLE_CLIENT_ID = "418581116582-qet8ant9k0t319hkd3640dtsa54eg07v.apps.googleusercontent.com"
  ADMIN_EMAIL = ""
  ADMIN_PASSWORD = ""

  MAIL_HOST = "smtp.gmail.com"
  MAIL_PORT = "587"
  MAIL_USERNAME = "fonght.dev@gmail.com"
  MAIL_PASSWORD = "yylmvpdohcxcvhzl"
  MAIL_FROM = "fonght.dev@gmail.com"

  PAYOS_ENABLED = "true"
  CHECKOUT_QR_ACCOUNT_NAME = "COMPASSED"
  PAYOS_CLIENT_ID = "22709ee0-2bae-4f51-890c-476e614d7324"
  PAYOS_API_KEY = "121b07c3-8f97-42fb-9f06-3ed6a5a624be"
  PAYOS_CHECKSUM_KEY = "a321cca7d82aafcd411cfe31079256c4bc9b5564ee98f3bdbb751d9ef8a62caa"
  PAYOS_BASE_URL = "https://api-merchant.payos.vn"
  PAYOS_RETURN_URL = "https://compassed.io.vn/checkout"
  PAYOS_CANCEL_URL = "https://compassed.io.vn/checkout"
  PAYOS_CHECK_COOLDOWN_SECONDS = "15"

  BE_PORT = "8080"
  FE_PORT = "3000"
  FE_API_BASE = "http://localhost:8080"
}
