from flask import Flask
from flask import render_template

app = Flask(__name__, template_folder="template", static_folder=".", static_url_path="")


@app.route("/")
def index():
    return render_template("landingPage.html")


@app.route("/landing")
def landing():
    return render_template("landingPage.html")


@app.route("/auth")
def auth():
    return render_template("auth.html")


@app.route("/placement-test")
def placement_test():
    return render_template("placementTest.html")


@app.route("/placement-result")
def placement_result():
    return render_template("placementTestResult.html")


@app.route("/learning-roadmap")
def learning_roadmap():
    return render_template("coursesDetail.html")


@app.route("/history")
def history():
    return render_template("history.html")


@app.route("/admin-login")
def admin_login():
    return render_template("admin-login.html")


@app.route("/admin")
def admin():
    return render_template("admin/adminDashboard.html")


@app.route("/admin/users")
def admin_users():
    return render_template("admin.html")


@app.route("/admin/question-bank")
def admin_question_bank():
    return render_template("admin/adminQuestionBank.html")


@app.route("/test-api")
def test_api():
    return render_template("test-api.html")


@app.route("/payment")
def payment():
    return render_template("payment.html")


@app.route("/payment/callback")
def payment_callback():
    return render_template("payment.html")


@app.route("/roadmap")
def roadmap():
    return render_template("roadmap.html")


@app.route("/mini-test")
def mini_test():
    return render_template("mini-test.html")


@app.route("/final-test")
def final_test():
    return render_template("final-test.html")


@app.route("/test-helper")
def test_helper():
    return render_template("test-helper.html")


if __name__ == "__main__":
    app.run(debug=True, port=3000, host='0.0.0.0')
