(ns datepicker.utils
  (:require-macros [javelin.core :refer [cell=]])
  (:require [cljs-time.core :as tc]
            [cljs-time.format :as tf]
            [datepicker.defaults :refer [days-in-week
                                         default-date-format
                                         default-time-format]]))

(defn formatter [f]
  (cond
    (string? f) (tf/formatter f)
    (keyword? f) (tf/formatters f)))

(defn year-month [d] (if d (tf/unparse (formatter "MMM - yyyy") d)))

(defn date-lense [state f]
  (cell= (when (-> state empty? not)
           (tf/parse (formatter f) state))
         #(reset! state (tf/unparse (formatter f) (tc/to-default-time-zone %)))))

(defn date-display-lense [state f]
  (cell= (when (-> state nil? not)
           (.log js/console state)
           (tf/unparse (formatter f) state))
         #(reset! state (tf/parse (formatter f) (tc/to-default-time-zone %)))))

(defn range-inc
  ([inc] (range-inc (tc/at-midnight (tc/now)) inc))
  ([start inc]
   {:start start
    :end (-> start (tc/plus inc) tc/at-midnight)}))

(defn set-dt-items [dt items]
  (let [dt-split {:year (tc/year dt) :month (tc/month dt) :day (tc/day dt)
                  :hour (tc/hour dt) :minute (tc/minute dt)
                  :second (tc/second dt) :milli (tc/milli dt)}
        {:keys [year month day hour minute second milli]} (merge dt-split items)]
    (tc/date-time year month day hour minute second milli)))

(defn merge-date-time [date time]
  (set-dt-items date {:hour (tc/hour time)
                      :minute (tc/minute time)}))

(defn date-with-time
  ([date time] (date-with-time date time {}))
  ([date time
    {:keys [date-format time-format]
     :or {date-format default-date-format
          time-format default-time-format}}]
   (cell= (when (and (-> date empty? not) (-> time empty? not))
            (let [d (tf/parse (tf/formatters date-format) date)
                  t (tf/parse (tf/formatters time-format) time)]
              (tf/unparse (tf/formatters :date-time) (merge-date-time d t)))))))

(defn round-mins [min inc] (* (.ceil js/Math (/ min inc)) inc))
