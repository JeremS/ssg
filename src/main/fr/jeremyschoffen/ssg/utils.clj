(ns fr.jeremyschoffen.ssg.utils)


(defn temp-id-maker []
  (let [current (atom 0)]
    (fn next-temp-id []
      (swap! current dec))))

(comment
  (def temp-id (temp-id-maker))
  (temp-id) := -1
  (temp-id) := -2
  (temp-id) := -3)

