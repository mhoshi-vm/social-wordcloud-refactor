CREATE OR REPLACE FUNCTION vader_sentiment(text TEXT)
    RETURNS jsonb
    LANGUAGE plpython3u
AS $$
/*[# th:insert="~{${plpythonscript}}"]*/ return 0 /*[/]*/
$$
