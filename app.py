from flask import Flask, jsonify, render_template
import requests

app = Flask(__name__)

GOOGLE_API_KEY = "YOUR_GOOGLE_MAPS_API_KEY"

def get_google_reviews(place_id):
    url = f"https://maps.googleapis.com/maps/api/place/details/json?place_id={place_id}&fields=name,rating,reviews,geometry&key={GOOGLE_API_KEY}"
    res = requests.get(url).json()
    if res.get("result"):
        data = res["result"]
        reviews = []
        for r in data.get("reviews", [])[:3]:
            reviews.append({"author": r["author_name"], "text": r["text"], "rating": r["rating"]})
        return {
            "school": data["name"],
            "rating": data.get("rating", 0),
            "reviews": reviews,
            "location": data.get("geometry", {}).get("location", {})
        }
    return None

def get_admission_scores(school_name):
    # Giả lập dữ liệu điểm chuẩn
    mock_data = {
        "Đại học Bách Khoa Hà Nội": {"2024": {"CNTT": 28.5, "Kinh tế": 26.1}, "2023": {"CNTT": 28.15, "Kinh tế": 25.6}},
        "Đại học Kinh tế Quốc dân": {"2024": {"Marketing": 27.4, "Tài chính": 26.8}, "2023": {"Marketing": 27.1, "Tài chính": 26.2}},
        "Đại học Quốc Gia TP.HCM": {"2024": {"CNTT": 27.9, "Kỹ thuật": 26.5}, "2023": {"CNTT": 27.6, "Kỹ thuật": 26.1}}
    }
    return mock_data.get(school_name)

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/api/universities')
def universities():
    schools = {
        "Đại học Bách Khoa Hà Nội": "ChIJW2aP5pwrNTERBbb6kld2x3A",
        "Đại học Kinh tế Quốc dân": "ChIJZ2nWvh8rNTERtzC6R_6CykE",
        "Đại học Quốc Gia TP.HCM": "ChIJX1fyEIVSNDER5VL-JRox5xE"
    }
    result = []
    for name, pid in schools.items():
        info = get_google_reviews(pid)
        if info:
            info["admission_scores"] = get_admission_scores(name)
            result.append(info)
    return jsonify(result)

if __name__ == '__main__':
    app.run(debug=True)
