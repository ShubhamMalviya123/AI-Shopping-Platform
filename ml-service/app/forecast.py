"""Step 5: price trend forecast with linear regression (swap for Prophet/XGBoost when you have more history)."""
import numpy as np
from sklearn.linear_model import LinearRegression

def forecast(history: list, months: int = 3):
    if len(history) < 4:
        return None
    x = np.arange(len(history)).reshape(-1, 1)
    model = LinearRegression().fit(x, history)
    future = model.predict(np.arange(len(history), len(history) + months).reshape(-1, 1))
    slope_pct = float(model.coef_[0] / np.mean(history) * 100)
    trend = "falling" if slope_pct < -0.8 else "rising" if slope_pct > 0.8 else "stable"
    advice = {"falling": "Prices have been dropping. Waiting may save a little more.",
              "rising": "Prices are creeping up. Buying now is safer.",
              "stable": "Price is steady. Buy whenever you are ready."}[trend]
    return {"values": [round(float(v)) for v in future], "trend": trend, "advice": advice}
