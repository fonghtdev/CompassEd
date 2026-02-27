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

@app.route("/roadmap-dashboard")
def roadmap_dashboard():
    return render_template("roadmapDashboard.html")


@app.route("/history")
def history():
    return render_template("history.html")

@app.route("/checkout")
def checkout():
    return render_template("checkout.html")

@app.route("/dashboard")
def dashboard():
    return render_template("dashboard.html")

@app.route("/profile")
def profile():
    return render_template("profile.html")


@app.route("/admin-login")
def admin_login():
    return render_template("admin/admin-login.html")


@app.route("/admin-dashboard")
def admin_dashboard():
    return render_template("admin/adminDashboard.html")

@app.route("/admin/question-bank")
def admin_question_bank():
    return render_template("admin/adminQuestionBank.html")
if __name__ == "__main__":
    app.run(debug=True, port=3000)

